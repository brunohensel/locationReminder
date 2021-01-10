package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private val markers = MutableLiveData<List<Marker>>()
    private val markersArray = arrayListOf<Marker>()
    private var isPermissionGranted: Boolean = true
    private lateinit var poiMarker: Marker
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        if (isPermissionDenied()) {
            isPermissionGranted = false
            checkPermissions()
        } else {
            isPermissionGranted = true
        }
        mapFragment.getMapAsync(this)


        binding.btnSavePoi.setOnClickListener {
            with(markers.value?.first()) {
                _viewModel.reminderSelectedLocationStr.value = this?.title
                _viewModel.latitude.value = this?.position?.latitude
                _viewModel.longitude.value = this?.position?.longitude
            }
            findNavController().popBackStack()
        }

        markers.observe(viewLifecycleOwner, {
            binding.btnSavePoi.isEnabled = it.isNotEmpty()
        })
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            _viewModel.selectedPOI.value?.let {
                if (it.placeId == poi.placeId) {
                    poiMarker.remove()
                    markersArray.clear()
                    markers.value = markersArray
                    _viewModel.selectedPOI.value = null
                }
            }
            if (markersArray.isNotEmpty()) {
                _viewModel.showSnackBar.value = "The total of POI or location have to be 1"
            } else {
                _viewModel.selectedPOI.value = poi
            }
        }
    }

    private fun setMarker(map: GoogleMap) {
        map.setOnMapLongClickListener { latLong ->
            if (markersArray.isNotEmpty()) {
                _viewModel.showSnackBar.value = "The total of POI or location have to be 1"
            } else {
                _viewModel.selectedLocation.value = latLong
            }
        }
    }

    private fun removeMarker(map: GoogleMap) {
        map.setOnMarkerClickListener {
            if (markersArray.isNotEmpty()) {
                clearMarkersAndResetLiveData()
            }
            true
        }
    }

    private fun clearMarkersAndResetLiveData() {
        map.clear()
        markersArray.clear()
        markers.value = markersArray
        _viewModel.selectedLocation.value = null
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(javaClass.name, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(javaClass.name, "Can't find style. Error: ", e)
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.isMyLocationEnabled = isPermissionGranted
        val zomLevel = 15f
        LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation.addOnSuccessListener {
            val homeLatLong = LatLng(it.latitude, it.longitude)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLong, zomLevel))
        }
        setPoiClick(map)
        setMarker(map)
        removeMarker(map)
        setMapStyle(map)

        _viewModel.selectedPOI.observe(viewLifecycleOwner, { poi ->
            poi?.let {
                poiMarker = map.addMarker(
                    MarkerOptions()
                        .position(it.latLng)
                        .title(it.name)
                )
                markersArray.add(poiMarker)
                markers.value = markersArray
                poiMarker.showInfoWindow()
            }
        })

        _viewModel.selectedLocation.observe(viewLifecycleOwner, { latLong ->
            latLong?.let {
                poiMarker = map.addMarker(
                    MarkerOptions()
                        .position(it)
                        .title(getString(R.string.dropped_pin))
                )
                markersArray.add(poiMarker)
                markers.value = markersArray
            }
        })
    }
}
