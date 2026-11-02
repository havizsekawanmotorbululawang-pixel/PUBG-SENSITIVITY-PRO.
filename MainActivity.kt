package com.pubgsensitivitypro

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlin.math.roundToInt

private val Ink = Color(0xFF090A0D)
private val Panel = Color(0xFF17191F)
private val Red = Color(0xFFE53935)
private val Gold = Color(0xFFFFC857)
private val White = Color(0xFFF4F4F5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme(background=Ink, surface=Panel, primary=Red, secondary=Gold)) { App() } }
    }
}

private val scopes = listOf("TPP No Scope","FPP No Scope","Red Dot / Holo","2x Scope","3x Scope","4x Scope","6x Scope","8x Scope")
private val categories = listOf("Camera","ADS","Gyroscope","ADS Gyroscope")
private val styles = listOf("Full Gyroscope","No Gyroscope","2 Finger","3 Finger","4 Finger Claw","5 Finger Claw","6 Finger Claw","Rush Aggressive","Spray Stabil","Sniper Player")
private val templates = listOf("iPhone Style","Android Low End","Android Mid Range","Android High End","Anti Recoil","Full Gyroscope","No Gyroscope","M416 Spray","AKM","Sniper")

@Composable fun App() {
    var tab by remember { mutableStateOf("Dashboard") }
    var device by remember { mutableStateOf("${Build.MANUFACTURER} ${Build.MODEL}") }
    var style by remember { mutableStateOf(styles.first()) }
    var profile by remember { mutableStateOf("Default profile") }
    var values by remember { mutableStateOf(categories.associateWith { scopes.associateWith { 100f }.toMutableMap() }.toMutableMap()) }
    var saved by remember { mutableStateOf(listOf<String>()) }
    var template by remember { mutableStateOf("Android Mid Range") }
    val clipboard = LocalClipboardManager.current
    val allText = categories.joinToString("\n") { c -> "$c\n" + scopes.joinToString("\n") { s -> "$s: ${values[c]?.get(s)?.roundToInt() ?: 100}" } }

    Scaffold(containerColor=Ink, bottomBar={
        NavigationBar(containerColor=Panel) {
            listOf("Dashboard","Generator","Templates","Profiles").forEach { item ->
                NavigationBarItem(selected=tab==item,onClick={tab=item},icon={},label={Text(item)})
            }
        }
    }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item {
                Text("PUBG SENSITIVITY PRO", color=Gold, fontWeight=FontWeight.Black, fontSize=22.sp)
                Text("SENSITIVITY LAB  •  OFFLINE", color=White.copy(.65f), fontSize=11.sp)
            }
            when(tab) {
                "Dashboard" -> {
                    item { Card(Modifier.fillMaxWidth(), colors=CardDefaults.cardColors(containerColor=Panel)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("DEVICE PROFILE", color=Gold, fontSize=12.sp)
                            Text(device, color=White, fontWeight=FontWeight.Bold, fontSize=18.sp)
                            OutlinedTextField(device,{device=it},label={Text("Merek / tipe HP")},modifier=Modifier.fillMaxWidth())
                            Text("Deteksi perangkat: ${Build.MANUFACTURER} ${Build.MODEL}", color=White.copy(.6f), fontSize=12.sp)
                        }
                    } }
                    item { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        Stat("4","Kategori"); Stat("32","Nilai"); Stat("Offline","Mode")
                    } }
                    item { Text("Sensitivity values",color=Gold,fontWeight=FontWeight.Bold) }
                    categories.forEach { category -> item { SensitivityCard(category, scopes, values[category]!!){ scope,v -> values[category] = values[category]!!.toMutableMap().also { it[scope]=v } } } }
                    item { Button(onClick={clipboard.setText(AnnotatedString(allText))},modifier=Modifier.fillMaxWidth()) { Text("COPY ALL") } }
                    item { OutlinedButton(onClick={values=categories.associateWith { scopes.associateWith { 100f }.toMutableMap() }.toMutableMap()},modifier=Modifier.fillMaxWidth()) { Text("RESET SETTINGS") } }
                }
                "Generator" -> {
                    item { Text("Automatic recommendation",color=Gold,fontWeight=FontWeight.Bold,fontSize=20.sp) }
                    item { Text("Hasil adalah titik awal yang perlu diuji dan disesuaikan—bukan jaminan cocok untuk semua pemain.",color=White.copy(.75f)) }
                    item { OutlinedTextField(device,{device=it},label={Text("Tipe HP")},modifier=Modifier.fillMaxWidth()) }
                    item { OutlinedTextField("RAM / chipset / ukuran layar",{},label={Text("Spesifikasi (opsional)")},modifier=Modifier.fillMaxWidth()) }
                    item { OutlinedTextField("90",{},label={Text("Refresh rate (Hz)")},modifier=Modifier.fillMaxWidth()) }
                    item { OutlinedTextField("60",{},label={Text("FPS game")},modifier=Modifier.fillMaxWidth()) }
                    item { Text("Gaya bermain",color=Gold) }
                    item { StylePicker(style,{style=it}) }
                    item { Button(onClick={
                        val base = when(style) { "Rush Aggressive"->135f; "Sniper Player"->70f; "No Gyroscope"->100f; else->110f }
                        values=categories.associateWith { c -> scopes.mapIndexed { i,s -> s to (if(c.contains("Gyroscope") && style=="No Gyroscope") 0f else (base-i*7).coerceIn(0f,300f)) }.toMap().toMutableMap() }.toMutableMap()
                        tab="Dashboard"
                    },modifier=Modifier.fillMaxWidth()) { Text("GENERATE RECOMMENDATION") } }
                }
                "Templates" -> {
                    item { Text("Template library",color=Gold,fontWeight=FontWeight.Bold,fontSize=20.sp) }
                    items(templates) { t -> Card(colors=CardDefaults.cardColors(containerColor=Panel),modifier=Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically) { Text(t,Modifier.weight(1f),color=White); TextButton(onClick={template=t; tab="Dashboard"}) { Text("Use") }; TextButton(onClick={saved=saved+t}) { Text("Save") } }
                    } }
                    item { Text("Template terpilih: $template",color=Gold) }
                    item { Text("Template dasar tersedia sebagai preset nama. Untuk mengembangkan, hubungkan template ke entitas Room dan implementasikan export/import JSON.",color=White.copy(.6f),fontSize=12.sp) }
                }
                else -> {
                    item { Text("Saved profiles",color=Gold,fontWeight=FontWeight.Bold,fontSize=20.sp) }
                    item { OutlinedTextField(profile,{profile=it},label={Text("Nama profil HP")},modifier=Modifier.fillMaxWidth()) }
                    item { Button(onClick={saved=saved+profile},modifier=Modifier.fillMaxWidth()) { Text("SAVE PROFILE") } }
                    items(saved) { Text("• $it",color=White) }
                    item { Text("Comparison: buat dua profil tersimpan lalu bandingkan nilai di layar ini. Data saat ini tersimpan selama sesi aplikasi.",color=White.copy(.6f),fontSize=12.sp) }
                }
            }
        }
    }
}

@Composable private fun Stat(value:String,label:String) {
    Card(Modifier.weight(1f),colors=CardDefaults.cardColors(containerColor=Panel)) { Column(Modifier.padding(12.dp)) { Text(value,color=Gold,fontWeight=FontWeight.Bold); Text(label,color=White.copy(.6f),fontSize=11.sp) } }
}

@Composable private fun SensitivityCard(title:String, scopes:List<String>, values:Map<String,Float>, onChange:(String,Float)->Unit) {
    var expanded by remember { mutableStateOf(title=="Camera") }
    Card(colors=CardDefaults.cardColors(containerColor=Panel),modifier=Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) { Text(title,color=Gold,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f)); TextButton(onClick={expanded=!expanded}) { Text(if(expanded)"Hide" else "Edit") } }
            if(expanded) scopes.forEach { scope ->
                val v=values[scope]?:100f
                Text("$scope  •  ${v.roundToInt()}",color=White,fontSize=13.sp)
                Slider(value=v,valueRange=0f..300f,onValueChange={onChange(scope,it)})
            }
        }
    }
}

@Composable private fun StylePicker(value:String,onChange:(String)->Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick={expanded=true}) { Text(value) }
        DropdownMenu(expanded=expanded,onDismissRequest={expanded=false}) {
            styles.forEach { DropdownMenuItem(text={Text(it)},onClick={onChange.also { expanded=false }) }
        }
    }
}
