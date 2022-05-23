package com.tallercmovil.ejercicio2

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.lang.IllegalArgumentException
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.util.logging.Level.parse

class QR : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private val PERMISO_CAMARA = 1
    private var scannerView: ZXingScannerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerView = ZXingScannerView(this)
        setContentView(scannerView)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checarPermiso()){
                //se concedió el permiso
            }else{
                solicitarPermiso()
            }
        }

        scannerView?.setResultHandler(this)
        scannerView?.startCamera()
    }

    private fun solicitarPermiso() {
        ActivityCompat.requestPermissions(this@QR, arrayOf(Manifest.permission.CAMERA),PERMISO_CAMARA)
    }

    private fun checarPermiso(): Boolean {
        return (ContextCompat.checkSelfPermission(this@QR, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    override fun handleResult(p0: Result?) {
        //código QR leído
        var scanResult = p0?.text

        Log.d("QR_LEIDO", scanResult!!)

        try{
            if(scanResult.contains("http",true)){
                val url = URL(scanResult)
                val i = Intent(Intent.ACTION_VIEW)
                i.setData(Uri.parse(scanResult))
                startActivity(i)
                finish()
            }else if (scanResult.contains("vcard",true)){
                Log.d("QR: ","Se leyo una VCARD")
            }else if (scanResult.contains("MSG",true)){
                Log.d("QR: ","Se leyo un correo")
                var i = ""
                var tmp = 4
                var tmp2 = 0
                for(i in scanResult){
                    if(i.toString() == ":"){
                        break
                    }
                    tmp++
                }
                for(i in scanResult){
                    if(i.toString() == ";"){
                        break
                    }
                    tmp2++
                }
                var mail = scanResult.substring(tmp,tmp2)
                var emails = arrayOf(mail)
                var intent = Intent(Intent.ACTION_SENDTO)
                intent.setType("*/*")
                intent.setData(Uri.parse("mailto:"))
                intent.putExtra(Intent.EXTRA_EMAIL,emails)
                intent.putExtra(Intent.EXTRA_SUBJECT,"Prueba")
                intent.putExtra(Intent.EXTRA_TEXT,"texto de prueba")
                if (intent.resolveActivity(packageManager)!=null){
                    startActivity(intent)
                }else{
                    AlertDialog.Builder(this@QR)
                        .setTitle("Error")
                        .setMessage("No se encontro aplicación de correo")
                        .setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialogInterface, i ->
                            dialogInterface.dismiss()
                            finish()
                        })
                        .create()
                        .show()
                }

            }else if (scanResult.contains("sms",true)){
                /*var caracteres = scanResult.length
                var numero = scanResult.substring(0,15)
                var mensaje = scanResult.substring(16,caracteres)
                Log.d("QR_LEIDO2 ",numero)
                Log.d("QR_LEIDO2 ",mensaje)
                var uri = Uri.parse(numero)
                startActivity(Intent(Intent.ACTION_SEND,uri))*/
            }else{
                throw IllegalArgumentException("Código no válido")
            }
        }catch(e: IllegalArgumentException){
            AlertDialog.Builder(this@QR)
                .setTitle("Error")
                .setMessage("El código QR no es válido para la aplicación")
                .setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    finish()
                })
                .create()
                .show()
        }


    }

    override fun onResume() {
        super.onResume()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checarPermiso()){
                if(scannerView == null){
                    scannerView = ZXingScannerView(this)
                    setContentView(scannerView)
                }

                scannerView?.setResultHandler(this)
                scannerView?.startCamera()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scannerView?.stopCamera()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){

            PERMISO_CAMARA -> {
                if(grantResults.isNotEmpty()){
                    if(grantResults[0]!= PackageManager.PERMISSION_GRANTED){
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                                AlertDialog.Builder(this@QR)
                                    .setTitle("Permiso requerido")
                                    .setMessage("Se necesita acceder a la cámara para leer los códigos QR")
                                    .setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialogInterface, i ->
                                        requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISO_CAMARA)
                                    })
                                    .setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialogInterface, i ->
                                        dialogInterface.dismiss()
                                        finish()
                                    })
                                    .create()
                                    .show()
                            }else{
                                Toast.makeText(this@QR, "El permiso de la cámara no se ha concedido", Toast.LENGTH_LONG).show()
                                finish()
                            }
                        }
                    }
                }
            }

        }
    }

}