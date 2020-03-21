package dev.alexfranco.mtw.mapas

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val perFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val perCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val COIGO_PERMISO = 100

    var provLocationClient : FusedLocationProviderClient? = null
    var locationReq : LocationRequest? = null
    private var callback : LocationCallback? = null
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        provLocationClient = FusedLocationProviderClient(this)
        solLocationRequest()

        callback = object : LocationCallback() {

            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                if( mMap != null ){
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true

                    for (ubicacion in p0?.locations!!){
                        Toast.makeText(applicationContext, ubicacion.latitude.toString() + "," + ubicacion.longitude.toString(), Toast.LENGTH_LONG).show()
                        val miPosicion = LatLng(ubicacion.latitude,ubicacion.longitude)
                        mMap.addMarker(MarkerOptions().position(miPosicion).title("Esta es mi posición"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(miPosicion))
                    }
                }
            }
        }
    }

    private fun solLocationRequest() {
        locationReq = LocationRequest()
        locationReq?.interval = 10000
        locationReq?.fastestInterval = 5000
        locationReq?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
    }

    private fun validarPermisosUbicacion():Boolean{
        val ubicacionExacta = ActivityCompat.checkSelfPermission(this,perFineLocation) == PackageManager.PERMISSION_GRANTED
        val ubicacionNormal = ActivityCompat.checkSelfPermission(this,perCoarseLocation) == PackageManager.PERMISSION_GRANTED
        return ubicacionExacta && ubicacionNormal
    }

    @SuppressLint("MissingPermission")
    private fun getUbicacion(){
        provLocationClient?.requestLocationUpdates(locationReq,callback,null)
    }

    private fun solPermisos(){
        val provContexto = ActivityCompat.shouldShowRequestPermissionRationale(this,perFineLocation)
        if( provContexto )
            solPermiso()
        else
            solPermiso()
    }

    private fun solPermiso() {
        requestPermissions(arrayOf(perFineLocation,perCoarseLocation), COIGO_PERMISO)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when( requestCode ){
            COIGO_PERMISO->{
                if( grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    getUbicacion()
                }
                else{
                    Toast.makeText(this,"No diste el permiso para acceder a la ubicación", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun detenerActualizacionUbicacion(){
        provLocationClient?.removeLocationUpdates(callback)
    }

    override fun onStart() {
        super.onStart()
        if( validarPermisosUbicacion()){
            getUbicacion()
        }
        else{
            solPermisos()
        }
    }

    override fun onPause() {
        super.onPause()
        detenerActualizacionUbicacion()
    }
}
