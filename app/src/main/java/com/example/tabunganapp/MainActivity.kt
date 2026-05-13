package com.example.tabunganapp

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import com.example.tabunganapp.DataStoreManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Toast
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Visibility
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import android.app.TimePickerDialog
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import android.app.PendingIntent
import java.util.*
import androidx.compose.runtime.mutableStateListOf
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.tabunganapp.ui.theme.TabunganAppTheme


val BgPage    = Color(0xFFEEF2FF)
val BgSurface = Color(0xFFFFFFFF)
val BgInput   = Color(0xFFE8EDFF)
val BgSubtle  = Color(0xFFDDE5FF)

val Blue50  = Color(0xFFEDE7F6)
val Blue100 = Color(0xFFD1C4E9)
val Blue200 = Color(0xFFB39DDB)
val Blue300 = Color(0xFF9575CD)
val Blue400 = Color(0xFF7E57C2)
val Blue500 = Color(0xFF673AB7)
val Blue600 = Color(0xFF5E35B1)
val Blue700 = Color(0xFF512DA8)
val Blue800 = Color(0xFF4527A0)

val GreenSoft  = Color(0xFF00C853)   // tetap hijau untuk status sukses
val GreenBg    = Color(0xFFE8F5E9)
val OrangeSoft = Color(0xFFFF6D00)
val OrangeBg   = Color(0xFFFFF3E0)
val RedSoft    = Color(0xFFEF5350)

val TextPrimary   = Color(0xFF1A1A2E)
// ── Pink Theme Colors (Soft Pastel) ───────────────
val PinkBg       = Color(0xFFFFF0F4)   // background sangat soft
val PinkLight    = Color(0xFFFFD6E0)   // tab bar & highlight
val PinkMain     = Color(0xFFE91E8C)   // aksen utama (sedikit lebih cerah)
val PinkDark     = Color(0xFFD81B60)   // shadow/dark
val PinkPastel1  = Color(0xFFF8BBD0)   // pink card icon (lebih muda)
val PinkPastel2  = Color(0xFFE1BEE7)   // purple card icon (lebih muda)
val PinkPastel3  = Color(0xFFC8E6C9)   // green card icon (lebih muda)
val PinkText     = Color(0xFF37474F)   // dark gray text
val PinkSubText  = Color(0xFF9E9E9E)   // gray subtitle
val TextSecondary = Color(0xFF5C5C8A)
val TextHint      = Color(0xFFAAAAAA)
val White         = Color(0xFFFFFFFF)

val HeaderGrad = Brush.linearGradient(
    listOf(Color(0xFF6C63FF), Color(0xFF9575CD))
)
val BtnGrad = Brush.linearGradient(
    listOf(Color(0xFF6C63FF), Color(0xFF9575CD))
)
val GreenGrad = Brush.linearGradient(
    listOf(Color(0xFF4527A0), Color(0xFF7E57C2))
)


data class Celengan(
    val id: String = java.util.UUID.randomUUID().toString(),
    var nama: String,
    var target: Int,
    var terkumpul: Int = 0,
    var image: String? = null,
    var nominal: Int = 0,
    var jenis: String = "Harian",
    var notifAktif: Boolean = false,
    var jamNotif: String = "08:00",
    var hariNotif: List<String> = emptyList(),
    var riwayat: MutableList<Transaksi> = mutableStateListOf()
)

data class Transaksi(
    val tanggal: String,
    val nominal: Int,
    val tipe: String = "MASUK",
    val keterangan: String = ""
)

data class BadgeData(
    val icon: String,
    val label: String,
    val desc: String,
    val earned: Boolean
)

data class NotificationItem(
    val title: String,
    val message: String,
    val time: String,
    val type: String = "pengingat",   // "pengingat" | "aktivitas_masuk" | "aktivitas_keluar" | "target" | "progress"
    val isRead: Boolean = false,
    val date: String = "Hari ini"     // dipakai untuk pengelompokan
)

// ═══════════════════════════════════════════════════════════════════
//  ACTIVITY
// ═══════════════════════════════════════════════════════════════════

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        enableEdgeToEdge()
        setContent {
            TabunganAppTheme {
                App()
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  ALARM HELPER
// ═══════════════════════════════════════════════════════════════════

fun scheduleNotification(context: Context, jam: String, userId: String) {


    try {
        if (jam.isEmpty() || !jam.contains(":")) return
        val parts = jam.split(":")
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_MONTH, 1)
        }
        val intent = Intent(context, NotifReceiver::class.java)

        intent.putExtra("USER_ID", userId)

        val requestCode = userId.hashCode()

        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms())
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pending
                )
            else
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pending
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun cancelNotification(context: Context, userId: String) {
    val intent = Intent(context, NotifReceiver::class.java)

    val requestCode = userId.hashCode()

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
}

// ═══════════════════════════════════════════════════════════════════
//  APP NAVIGATOR
// ═══════════════════════════════════════════════════════════════════

@Composable
fun App() {

    val auth = FirebaseAuth.getInstance()

    val currentUser = auth.currentUser

    val userId = currentUser?.uid ?: ""

    var screen by remember { mutableStateOf("splash") }
    var currentNav by remember { mutableStateOf("home") }


    var selected by remember { mutableStateOf<Celengan?>(null) }
    var listCelengan by remember { mutableStateOf(mutableListOf<Celengan>()) }

    var isDataLoaded by remember { mutableStateOf(false) }

    fun updateList() {
        listCelengan = listCelengan.toMutableList()
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showNotification by remember {
        mutableStateOf(false)
    }


    LaunchedEffect(userId) {
        try {
            if (userId.isNotEmpty()) {
                listCelengan = FirestoreManager.loadCelengan(userId).toMutableList()
                isDataLoaded = true   // 🔥 TAMBAHAN PENTING
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

// SAVE ke Firestore — pakai snapshotFlow agar detect perubahan field dalam object
    LaunchedEffect(listCelengan.size, userId, isDataLoaded) {
        try {
            if (userId.isNotEmpty() && isDataLoaded && listCelengan.isNotEmpty()) {
                FirestoreManager.saveCelengan(userId, listCelengan)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            fadeIn(tween(280)) togetherWith fadeOut(tween(200))
        },
        label = "nav"
    ) { target ->

        if (showNotification) {

            // ✅ Load dari riwayat aktual yang disimpan NotifReceiver
            val notifications = remember(showNotification) {
                NotifHistoryManager.load(context)
            }

            NotificationScreen(
                notifications = notifications,
                onBack = {
                    showNotification = false
                }
            )

        } else {

            when (target) {

                "splash" -> SplashScreen {
                    if (auth.currentUser != null) {
                        screen = "home"
                    } else {
                        screen = "login"
                    }
                }

                "home" -> MainScreen(
                    currentNav = currentNav,
                    onNavChange = { currentNav = it },
                    list = listCelengan,
                    onTambah = { screen = "tambah" },
                    onProfil = { screen = "profil" },
                    onClickItem = {
                        selected = it
                        screen = "detail"
                    },
                    onLogout = {
                        val uid = auth.currentUser?.uid ?: ""
                        cancelNotification(context, uid)
                        auth.signOut()
                        screen = "login"
                    },
                    onBellClick = { showNotification = true }  // ← TAMBAHKAN INI
                )

                "tambah" -> TambahScreen(
                    onSimpan = {
                        listCelengan = (listCelengan + it).toMutableList()
                        screen = "home"
                    },
                    onKembali = {
                        screen = "home"
                    }
                )

                "detail" -> selected?.let {
                    DetailScreen(
                        celengan = it,
                        onUpdate = { updateList() },
                        onKembali = { screen = "home" },
                        onDelete = { cel ->
                            listCelengan =
                                listCelengan.filter { it != cel }.toMutableList()

                            screen = "home"
                        },
                        onEdit = {
                            screen = "edit"
                        }
                    )
                }

                "edit" -> selected?.let {
                    EditScreen(
                        celengan = it,
                        onSimpan = {
                            updateList()
                            screen = "detail"
                        },
                        onKembali = {
                            screen = "detail"
                        }
                    )
                }

                "login" -> LoginScreen(
                    onLoginSuccess = {
                        screen = "home"
                    },
                    onGoRegister = {
                        screen = "register"
                    }
                )

                "register" -> RegisterScreen(
                    onRegisterSuccess = {
                        screen = "home"
                    },
                    onGoLogin = {
                        screen = "login"
                    }
                )

                "profil" -> ProfileScreen(
                    listCelengan = listCelengan,
                    onKembali = {
                        screen = "home"
                    },
                    onLogout = {
                        val uid = auth.currentUser?.uid ?: ""
                        cancelNotification(context, uid)
                        auth.signOut()
                        screen = "login"
                    },
                    onNavChange = { nav ->
                        currentNav = nav
                        screen = "home"
                    },
                    onTambah = {
                        screen = "tambah"
                    }
                )
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════════
//  LOGIN SCREEN — Beautiful Blue White Design
// ═══════════════════════════════════════════════════════════════════

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoRegister: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFEEF2FF), Color(0xFFE8EDFF))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 60.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Logo + Nama App ──────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF6C63FF), Color(0xFF9575CD))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_wishpay),
                    contentDescription = "wishPay",
                    modifier = Modifier.size(44.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "wishPay",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Blue700,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Wujudkan impian finansial Anda\ndengan tenang.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(32.dp))

            // ── Card Form ────────────────────────────────────
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    // Email
                    Text(
                        "Email",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("nama@email.com", color = TextHint) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TextHint,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            unfocusedBorderColor = Color(0xFFDDE3EE),
                            cursorColor = Blue500,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = White,
                            unfocusedContainerColor = White
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    // Password
                    Text(
                        "Kata Sandi",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••", color = TextHint) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = TextHint,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = if (showPassword) Blue500 else TextHint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            unfocusedBorderColor = Color(0xFFDDE3EE),
                            cursorColor = Blue500,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = White,
                            unfocusedContainerColor = White
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(14.dp))

                    // ── Ingat Saya + Lupa Password ───────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                modifier = Modifier.scale(0.75f),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = White,
                                    checkedTrackColor = Blue500,
                                    uncheckedThumbColor = Color(0xFFCDD5E0),
                                    uncheckedTrackColor = Color(0xFFEEF2F8)
                                )
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Ingat Saya", fontSize = 13.sp, color = TextSecondary)
                        }
                        TextButton(onClick = {}) {
                            Text(
                                "Lupa Password?",
                                fontSize = 13.sp,
                                color = Blue600,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Tombol Masuk ──────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(
                                6.dp, RoundedCornerShape(14.dp),
                                ambientColor = Blue600.copy(0.3f), spotColor = Blue700.copy(0.3f)
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(Blue600)
                            .clickable(enabled = !isLoading) {
                                if (email.isNotEmpty() && password.isNotEmpty()) {
                                    isLoading = true
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnSuccessListener { onLoginSuccess() }
                                        .addOnFailureListener {
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                "Login gagal. Periksa email & password.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Email dan password tidak boleh kosong.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                "Masuk",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Divider ───────────────────────────────
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(0.5.dp)
                                .background(Color(0xFFDDE3EE))
                        )
                        Text("  Atau masuk dengan  ", fontSize = 12.sp, color = TextHint)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(0.5.dp)
                                .background(Color(0xFFDDE3EE))
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Google Button ─────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(White)
                            .border(1.dp, Color(0xFFDDE3EE), RoundedCornerShape(14.dp))
                            .clickable {
                                Toast.makeText(
                                    context,
                                    "Google login belum tersedia",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "G", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                color = Color(0xFF4285F4)
                            )
                            Text(
                                "Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Link Register ─────────────────────────
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Belum punya akun? ", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            "Daftar Sekarang",
                            fontSize = 13.sp,
                            color = Blue600,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onGoRegister() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Security note ─────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Notifications,
                    null,
                    tint = Blue400,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "Data Anda dilindungi dengan enkripsi tingkat bank",
                    fontSize = 11.sp,
                    color = Blue500,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  REGISTER SCREEN — Beautiful Blue White Design
// ═══════════════════════════════════════════════════════════════════

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onGoLogin: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var namaLengkap by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFEEF2FF), Color(0xFFE8EDFF))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // ── TOP BACK BUTTON ───────────────────────────────
            Box(
                modifier = Modifier
                    .padding(start = 20.dp, top = 52.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(White)
                    .border(0.5.dp, Color(0xFFDDE3EE), CircleShape)
                    .clickable { onGoLogin() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ArrowBack, null,
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── JUDUL ─────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Nama app biru besar
                Text(
                    "wishPay",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blue600,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    "Daftar Akun wishPay",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Mulai langkah pertama menuju kebebasan\nfinansial Anda hari ini.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(32.dp))

                // ── FORM FIELDS ───────────────────────────────

                // Nama Lengkap
                FormLabel("Nama Lengkap")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = namaLengkap,
                    onValueChange = { namaLengkap = it },
                    placeholder = {
                        Text(
                            "Masukkan nama sesuai KTP",
                            color = TextHint,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = cleanFieldColors(),
                    singleLine = true
                )

                Spacer(Modifier.height(18.dp))

                // Email
                FormLabel("Email")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = {
                        Text(
                            "contoh@email.com",
                            color = TextHint,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = cleanFieldColors(),
                    singleLine = true
                )

                Spacer(Modifier.height(18.dp))

                // Password
                FormLabel("Password")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = {
                        Text(
                            "Minimal 8 karakter",
                            color = TextHint,
                            fontSize = 14.sp
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = null,
                                tint = if (showPassword) Blue500 else TextHint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (showPassword)
                        VisualTransformation.None
                    else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = cleanFieldColors(),
                    singleLine = true
                )

                Spacer(Modifier.height(18.dp))

                // Konfirmasi Password
                FormLabel("Konfirmasi Password")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    placeholder = {
                        Text(
                            "Ulangi password Anda",
                            color = TextHint,
                            fontSize = 14.sp
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showConfirm = !showConfirm }) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = null,
                                tint = if (showConfirm) Blue500 else TextHint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (showConfirm)
                        VisualTransformation.None
                    else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = cleanFieldColors(),
                    singleLine = true
                )

                Spacer(Modifier.height(28.dp))

                // ── TOMBOL DAFTAR ─────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            6.dp,
                            RoundedCornerShape(14.dp),
                            ambientColor = Blue600.copy(0.3f),
                            spotColor = Blue700.copy(0.3f)
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(Blue600)
                        .clickable(enabled = !isLoading) {
                            when {
                                namaLengkap.isEmpty() ->
                                    Toast.makeText(
                                        context,
                                        "Nama lengkap tidak boleh kosong",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                email.isEmpty() ->
                                    Toast.makeText(
                                        context,
                                        "Email tidak boleh kosong",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                password.length < 6 ->
                                    Toast.makeText(
                                        context,
                                        "Password minimal 6 karakter",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                password != confirm ->
                                    Toast.makeText(
                                        context,
                                        "Password tidak sama",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                else -> {
                                    isLoading = true
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnSuccessListener { result ->
                                            // Simpan nama lengkap ke Firebase profile
                                            val updates =
                                                com.google.firebase.auth.UserProfileChangeRequest
                                                    .Builder()
                                                    .setDisplayName(namaLengkap)
                                                    .build()
                                            result.user?.updateProfile(updates)
                                                ?.addOnCompleteListener {
                                                    isLoading = false
                                                    onRegisterSuccess()
                                                }
                                        }
                                        .addOnFailureListener {
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                "Registrasi gagal: ${it.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            "Daftar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── LINK KE LOGIN ─────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Sudah punya akun? ",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        "Masuk",
                        fontSize = 14.sp,
                        color = Blue600,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onGoLogin() }
                    )
                }

                Spacer(Modifier.height(32.dp))

                // ── SECURE ENCRYPTION BOX ─────────────────────
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Garis + teks SECURE ENCRYPTION
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(0.5.dp)
                                .background(Color(0xFFDDE3EE))
                        )
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                null,
                                tint = TextHint,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                "SECURE ENCRYPTION",
                                fontSize = 10.sp,
                                color = TextHint,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(0.5.dp)
                                .background(Color(0xFFDDE3EE))
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Data Anda dilindungi dengan enkripsi tingkat perbankan\n(AES-256). Kami tidak akan membagikan informasi pribadi\nAnda tanpa izin.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── 2 ikon shield + key ────────────────────
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Shield icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEEF2F8))
                                .border(0.5.dp, Color(0xFFDDE3EE), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // Key icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEEF2F8))
                                .border(0.5.dp, Color(0xFFDDE3EE), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  KOMPONEN REUSABLE — Bear Mascot & Auth TextField
// ═══════════════════════════════════════════════════════════════════

@Composable
fun BearMascot(isHappy: Boolean) {
    Box(
        modifier = Modifier
            .size(if (isHappy) 110.dp else 130.dp)
            .shadow(
                16.dp,
                CircleShape,
                ambientColor = Blue300.copy(0.2f),
                spotColor = Blue400.copy(0.2f)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Lingkaran background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(Blue100.copy(0.6f), Blue50.copy(0.3f)))
                )
        )
        // Emoji beruang
        Text(
            text = if (isHappy) "🐻🎉" else "🐻",
            fontSize = if (isHappy) 44.sp else 60.sp,
            modifier = Modifier.offset(y = if (isHappy) (-2).dp else 0.dp)
        )
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingEmoji: String,
    isPassword: Boolean,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Blue600,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF4F9FE))
                .border(
                    width = 2.dp,
                    color = Blue200.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(placeholder, color = Blue200, fontSize = 13.sp)
                },
                visualTransformation = if (isPassword && !showPassword)
                    PasswordVisualTransformation() else VisualTransformation.None,
                leadingIcon = {
                    Text(
                        leadingEmoji, fontSize = 16.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                },
                trailingIcon = if (isPassword && onTogglePassword != null) ({
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            tint = if (showPassword) Blue500 else Blue200,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }) else null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = Blue500,
                    focusedTextColor = Color(0xFF1A5A9A),
                    unfocusedTextColor = Color(0xFF1A5A9A)
                ),
                singleLine = true
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  KOMPONEN REUSABLE
// ═══════════════════════════════════════════════════════════════════

/** Card bersih dengan shadow halus */
@Composable
fun CleanCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
fun FormLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Gray
    )
}

@Composable
fun cleanFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Blue500,
    unfocusedBorderColor = Color.LightGray,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = Blue500
)

/** Progress bar biru bersih */
@Composable
fun CleanProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val anim by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(700, easing = EaseOutCubic),
        label = "bar"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(BgSubtle)
            .height(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(anim)
                .clip(RoundedCornerShape(50))
                .background(BtnGrad)
        )
    }
}

/** Circular arc progress */
@Composable
fun CircularProgress(
    progress: Float,
    size: Int = 64,
    strokeWidth: Float = 7f,
    modifier: Modifier = Modifier
) {
    val anim by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "arc"
    )
    Canvas(modifier = modifier.size(size.dp)) {
        val sw = strokeWidth
        val sweep = anim * 360f
        val padding = sw / 2
        val topLeft = androidx.compose.ui.geometry.Offset(padding, padding)
        val arcSize = androidx.compose.ui.geometry.Size(
            this.size.width - sw,
            this.size.height - sw
        )
        // Track
        drawArc(
            color = BgSubtle,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = androidx.compose.ui.graphics.drawscope.Stroke(sw, cap = StrokeCap.Round)
        )
        // Fill
        if (sweep > 0f) {
            drawArc(
                color = Blue400,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(sw, cap = StrokeCap.Round)
            )
        }
    }
}

/** Pill badge / tag */
@Composable
fun Badge(
    text: String,
    bgColor: Color = Blue50,
    textColor: Color = Blue600,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

// ═══════════════════════════════════════════════════════════════════
//  SPLASH SCREEN — desain wishPay (background biru muda, floating shapes)
// ═══════════════════════════════════════════════════════════════════

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2800)
        onFinish()
    }

    val infinite = rememberInfiniteTransition(label = "splash")

    // Animasi floating untuk shapes dekoratif
    val floatY1 by infinite.animateFloat(
        0f, -18f,
        infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "fy1"
    )
    val floatY2 by infinite.animateFloat(
        0f, 14f,
        infiniteRepeatable(
            tween(1800, delayMillis = 300, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "fy2"
    )
    val floatY3 by infinite.animateFloat(
        0f, -12f,
        infiniteRepeatable(
            tween(2500, delayMillis = 600, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "fy3"
    )
    val floatY4 by infinite.animateFloat(
        0f, 20f,
        infiniteRepeatable(
            tween(1900, delayMillis = 900, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "fy4"
    )
    val floatY5 by infinite.animateFloat(
        0f, -16f,
        infiniteRepeatable(
            tween(2100, delayMillis = 400, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "fy5"
    )

    val logoScale by infinite.animateFloat(
        0.96f, 1.04f,
        infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "logo"
    )

    val dotScale1 by infinite.animateFloat(
        0.5f,
        1.3f,
        infiniteRepeatable(tween(600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "d1"
    )
    val dotScale2 by infinite.animateFloat(
        0.5f,
        1.3f,
        infiniteRepeatable(
            tween(600, delayMillis = 200, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "d2"
    )
    val dotScale3 by infinite.animateFloat(
        0.5f,
        1.3f,
        infiniteRepeatable(
            tween(600, delayMillis = 400, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "d3"
    )

    // Warna dots: biru, hijau, merah muda (seperti di gambar)
    val dotColors = listOf(Blue400, GreenSoft, Color(0xFFEF5350))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFEEF2FF), Color(0xFFD8D0FF))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // ── Floating geometric shapes & coins ────────────────────
        // Koin kiri atas
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-10).dp, y = (100).dp + floatY1.dp)
                .size(90.dp)
                .alpha(0.55f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(Color(0xFFFFD54F), Color(0xFFFFA000)))
                )
        )
        // Segitiga biru kiri atas
        Canvas(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 40.dp, y = (150).dp + floatY2.dp)
                .size(50.dp)
                .alpha(0.45f)
        ) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width / 2f, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, color = Blue400)
        }
        // Koin kanan atas
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-20).dp, y = (80).dp + floatY3.dp)
                .size(75.dp)
                .alpha(0.5f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(Color(0xFFFFD54F), Color(0xFFFFA000)))
                )
        )
        // Segitiga hijau kanan atas
        Canvas(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-60).dp, y = (170).dp + floatY4.dp)
                .size(42.dp)
                .alpha(0.45f)
        ) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width / 2f, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, color = GreenSoft)
        }
        // Koin kiri bawah
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-5).dp, y = (-130).dp + floatY5.dp)
                .size(60.dp)
                .alpha(0.45f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(Color(0xFFFFD54F), Color(0xFFFFA000)))
                )
        )
        // Segitiga biru bawah kiri
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 50.dp, y = (-180).dp + floatY1.dp)
                .size(36.dp)
                .alpha(0.4f)
        ) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(size.width, 0f)
                lineTo(0f, 0f)
                close()
            }
            drawPath(path, color = Blue300)
        }
        // Koin kanan bawah
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-15).dp, y = (-100).dp + floatY2.dp)
                .size(80.dp)
                .alpha(0.5f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(Color(0xFFFFD54F), Color(0xFFFFA000)))
                )
        )
        // Segitiga merah kanan bawah
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-80).dp, y = (-160).dp + floatY3.dp)
                .size(44.dp)
                .alpha(0.4f)
        ) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(size.width, 0f)
                lineTo(0f, 0f)
                close()
            }
            drawPath(path, color = Color(0xFFEF5350).copy(alpha = 0.7f))
        }

        // ── Konten tengah ─────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo box (pakai image resource R.drawable.logo_wishpay)
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .shadow(
                        12.dp,
                        RoundedCornerShape(36.dp),
                        ambientColor = Blue400.copy(0.15f),
                        spotColor = Blue500.copy(0.2f)
                    )
                    .clip(RoundedCornerShape(36.dp))
                    .background(White)
                    .scale(logoScale),
                contentAlignment = Alignment.Center
            ) {
                // GANTI dengan logo kustom dari drawable
                // Pastikan kamu menempatkan file logo (misal: logo_wishpay.png) di res/drawable/
                Image(
                    painter = painterResource(id = R.drawable.logo_wishpay),
                    contentDescription = "wishPay logo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(28.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(32.dp))

            // Nama app — hanya "wishPay"
            Text(
                "wishPay",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = (-1).sp
            )

            Spacer(Modifier.height(10.dp))

            // Tagline dalam pill (seperti di gambar)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(White.copy(alpha = 0.85f))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    "✨ Impianmu satu langkah lagi!",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(48.dp))

            // Dots loading
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(dotScale1, dotScale2, dotScale3).forEachIndexed { i, scale ->
                    Box(
                        modifier = Modifier
                            .scale(scale)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColors[i])
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  MAIN SCREEN — wrapper dengan bottom navigation
// ═══════════════════════════════════════════════════════════════════

@Composable
fun MainScreen(
    currentNav: String,
    onNavChange: (String) -> Unit,
    list: List<Celengan>,
    onTambah: () -> Unit,
    onProfil: () -> Unit,
    onClickItem: (Celengan) -> Unit,
    onLogout: () -> Unit,
    onBellClick: () -> Unit = {}   // ← TAMBAHKAN INI
) {
    Scaffold(
        bottomBar = {
            WishPayBottomNav(
                currentNav = currentNav,
                onNavChange = onNavChange,
                onTambah = onTambah,
                onProfil = onProfil
            )
        },
        containerColor = Color(0xFFFFF0F4)
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            when (currentNav) {
                "home" -> HomeScreenContent(
                    list = list,
                    onClickItem = onClickItem,
                    onBellClick = onBellClick   // ← TAMBAHKAN INI
                )
                "statistik" -> StatistikScreen(list = list)
                "aktivitas" -> AktivitasScreen(list = list)
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════════
//  BOTTOM NAV — Pink Theme
// ═══════════════════════════════════════════════════════════════════
@Composable
fun WishPayBottomNav(
    currentNav: String,
    onNavChange: (String) -> Unit,
    onTambah: () -> Unit,
    onProfil: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF0F4))  // pink sangat soft seperti foto 2
    ) {
        // Garis tipis di atas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.8.dp)
                .background(Color(0xFFF2C8E0))
                .align(Alignment.TopCenter)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Beranda
            FlatNavItem(
                label = "Beranda",
                isActive = currentNav == "home",
                icon = Icons.Default.Home,
                onClick = { onNavChange("home") }
            )
            // Statistik
            FlatNavItem(
                label = "Statistik",
                isActive = currentNav == "statistik",
                icon = Icons.Default.BarChart,
                onClick = { onNavChange("statistik") }
            )
            // Tombol + tengah — seperti foto 2: bulat kecil pink
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE91E8C))   // pink cerah persis foto 2
                        .clickable { onTambah() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "Tambah",
                    fontSize = 10.sp,
                    color = Color(0xFFE91E8C),
                    fontWeight = FontWeight.Medium
                )
            }
            // Aktivitas
            FlatNavItem(
                label = "Aktivitas",
                isActive = currentNav == "aktivitas",
                icon = Icons.Default.History,
                onClick = { onNavChange("aktivitas") }
            )
            // Profil
            FlatNavItem(
                label = "Profil",
                isActive = false,
                icon = Icons.Default.Person,
                onClick = { onProfil() }
            )
        }
    }
}

@Composable
fun RowScope.FlatNavItem(
    label: String,
    isActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) Color(0xFFE91E8C) else Color(0xFFC0A0B8),
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) Color(0xFFE91E8C) else Color(0xFFC0A0B8)
        )
    }
}

@Composable
fun RowScope.PinkNavItem(
    label: String,
    isActive: Boolean,
    iconKey: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (isActive) PinkLight else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (iconKey) {
                "home"      -> Icons.Default.Home
                "statistik" -> Icons.Default.BarChart
                "aktivitas" -> Icons.Default.History
                else        -> Icons.Default.Person
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) PinkMain else PinkSubText,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) PinkMain else PinkSubText
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
//  HOME SCREEN CONTENT — Pink Theme (sesuai desain baru)
// ═══════════════════════════════════════════════════════════════════
@Composable
fun HomeScreenContent(
    list: List<Celengan>,
    onClickItem: (Celengan) -> Unit,
    onBellClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val totalTarget     = list.sumOf { it.target }
    val totalTerkumpul  = list.sumOf { it.terkumpul }
    val totalTercapai   = list.count { it.terkumpul >= it.target }

    val auth        = FirebaseAuth.getInstance()
    val email       = auth.currentUser?.email ?: ""
    val displayName = auth.currentUser?.displayName?.ifEmpty { null }
        ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
    val initials    = displayName.split(" ").take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")

    // Hitung kenaikan (jumlah masuk semua riwayat)
    val totalMasukBulanIni = list.sumOf { cel ->
        cel.riwayat.filter { it.tipe == "MASUK" }.sumOf { it.nominal }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFEF0F7))
    ) {
        // ── HEADER ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PinkBg)
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kiri: Avatar + Greeting
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(PinkMain),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initials.ifEmpty { "U" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column {
                    Text(
                        "Selamat pagi",
                        fontSize = 12.sp,
                        color = PinkSubText
                    )
                    Text(
                        displayName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PinkText
                    )
                }
            }

            // Kanan: Bell icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onBellClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications, null,
                    tint = PinkMain,
                    modifier = Modifier.size(20.dp)
                )
                val notifCount = list.count { it.notifAktif }
                if (notifCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-1).dp, y = 2.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF5350))
                    )
                }
            }
        }

        // ── SCROLLABLE CONTENT ───────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── TOTAL TABUNGAN CARD ──────────────────────────────
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "TOTAL TABUNGAN",
                        fontSize = 11.sp,
                        color = PinkSubText,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Rp ${"%,d".format(totalTerkumpul).replace(",", ".")}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = PinkText
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("↑ ", fontSize = 13.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        Text(
                            "Naik Rp ${"%,d".format(totalMasukBulanIni).replace(",", ".")} bulan ini",
                            fontSize = 12.sp,
                            color = PinkSubText
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── 3 STAT CARDS ────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PinkStatCard(
                    modifier = Modifier.weight(1f),
                    iconBgColor = PinkPastel1,
                    label = "Terkumpul",
                    value = if (totalTerkumpul >= 1_000_000)
                        "Rp ${String.format("%.2f", totalTerkumpul / 1_000_000f)} jt"
                    else "Rp ${"%,d".format(totalTerkumpul).replace(",", ".")}"
                )
                PinkStatCard(
                    modifier = Modifier.weight(1f),
                    iconBgColor = PinkPastel2,
                    label = "Target Total",
                    value = if (totalTarget >= 1_000_000)
                        "Rp ${String.format("%.1f", totalTarget / 1_000_000f)} jt"
                    else "Rp ${"%,d".format(totalTarget).replace(",", ".")}"
                )
                PinkStatCard(
                    modifier = Modifier.weight(1f),
                    iconBgColor = PinkPastel3,
                    label = "Tercapai",
                    value = "$totalTercapai target"
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── TAB BAR ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .background(PinkLight)
                    .padding(4.dp)
            ) {
                listOf("Berlangsung", "Tercapai").forEachIndexed { idx, label ->
                    val selected = selectedTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (selected) Color.White else Color.Transparent)
                            .clickable { selectedTab = idx }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) PinkText else PinkMain
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── LIST TABUNGAN ────────────────────────────────────
            val filteredList = if (selectedTab == 0)
                list.filter { it.terkumpul < it.target }
            else
                list.filter { it.terkumpul >= it.target }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🐷", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (selectedTab == 0) "Belum ada tabungan"
                            else "Belum ada yang tercapai",
                            fontSize = 15.sp,
                            color = PinkText,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tap tombol + untuk mulai menabung",
                            fontSize = 12.sp,
                            color = PinkSubText
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    filteredList.forEachIndexed { index, item ->
                        PinkCelenganCard(
                            item  = item,
                            index = index,
                            onClick = { onClickItem(item) }
                        )
                    }
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}

// ── Komponen StatCard kecil ────────────────────────────────────────
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: String,
    iconBg: Color,
    label: String,
    value: String,
    isUnderlined: Boolean,
    labelColor: Color = TextSecondary,  // TAMBAH INI
    valueColor: Color = TextPrimary      // TAMBAH INI
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isUnderlined) 2.dp else 0.5.dp,
            if (isUnderlined) Blue600 else Color(0xFFDDE3EE)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                fontSize = 9.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                letterSpacing = 0.3.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  PINK BAR CHART — Setor per Bulan
// ═══════════════════════════════════════════════════════════════════
@Composable
fun PinkBarChart(list: List<Celengan>) {
    val bulanLabels = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Agu", "Sep", "Okt", "Nov", "Des")

    // Ambil 5 bulan terakhir
    val calendar = java.util.Calendar.getInstance()
    val currentMonth = calendar.get(java.util.Calendar.MONTH) // 0-based

    val last5Months = (4 downTo 0).map { offset ->
        val monthIdx = (currentMonth - offset + 12) % 12
        monthIdx
    }

    // Hitung total masuk per bulan dari riwayat
    val dataPerBulan = last5Months.map { monthIdx ->
        val total = list.sumOf { cel ->
            cel.riwayat.filter { trx ->
                trx.tipe == "MASUK" && try {
                    val sdf = java.text.SimpleDateFormat("dd MMM yyyy", Locale("id"))
                    val date = sdf.parse(trx.tanggal.substringBefore(" •").trim())
                    val cal = java.util.Calendar.getInstance()
                    cal.time = date!!
                    cal.get(java.util.Calendar.MONTH) == monthIdx
                } catch (e: Exception) { false }
            }.sumOf { it.nominal }
        }
        Pair(bulanLabels[monthIdx], total)
    }

    val maxVal = dataPerBulan.maxOfOrNull { it.second } ?: 1
    val currentMonthLabel = bulanLabels[currentMonth]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        dataPerBulan.forEach { (label, value) ->
            val isCurrentMonth = label == currentMonthLabel
            val heightFraction = if (maxVal > 0) (value.toFloat() / maxVal) else 0f
            val animatedHeight by animateFloatAsState(
                targetValue = heightFraction.coerceIn(0.05f, 1f),
                animationSpec = tween(800, easing = EaseOutCubic),
                label = "bar_$label"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .fillMaxHeight(animatedHeight)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            if (isCurrentMonth) Color(0xFFE91E8C)
                            else Color(0xFFF8BBD0)
                        )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    label,
                    fontSize = 10.sp,
                    color = if (isCurrentMonth) PinkMain else PinkSubText,
                    fontWeight = if (isCurrentMonth) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  STATISTIK SCREEN
// ═══════════════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════════════
//  STATISTIK SCREEN — Pink Theme
// ═══════════════════════════════════════════════════════════════════
@Composable
fun StatistikScreen(list: List<Celengan>) {
    val totalMasuk   = list.sumOf { cel -> cel.riwayat.filter { it.tipe == "MASUK" }.sumOf { it.nominal } }
    val totalKeluar  = list.sumOf { cel -> cel.riwayat.filter { it.tipe == "KELUAR" }.sumOf { it.nominal } }
    val totalTrxMasuk = list.sumOf { cel -> cel.riwayat.count { it.tipe == "MASUK" } }
    val totalTrxKeluar = list.sumOf { cel -> cel.riwayat.count { it.tipe == "KELUAR" } }
    val totalAktif   = list.count { it.terkumpul < it.target }
    val totalBulan   = 10 // estimasi
    val rataRata     = if (totalBulan > 0) totalMasuk / totalBulan else 0

    val progressColors = listOf(
        Color(0xFFEC407A),  // pink
        Color(0xFF9C27B0),  // purple
        Color(0xFFFF9800),  // orange
        Color(0xFF4CAF50),  // green
        Color(0xFF2196F3)   // blue
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PinkBg)
    ) {
        // ── HEADER ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(PinkLight)
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 24.dp)
        ) {
            Column {
                Text(
                    "Statistik",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PinkMain
                )
                Text(
                    "Ringkasan tabunganmu",
                    fontSize = 13.sp,
                    color = PinkMain.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // ── KONTEN SCROLLABLE ────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── 4 STAT CARDS 2x2 ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Disetor
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Total Disetor",
                            fontSize = 12.sp,
                            color = PinkSubText
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (totalMasuk >= 1_000_000)
                                "Rp ${String.format("%.2f", totalMasuk / 1_000_000f)} jt"
                            else "Rp ${"%,d".format(totalMasuk).replace(",", ".")}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PinkText
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Dari $totalTrxMasuk setor",
                            fontSize = 11.sp,
                            color = PinkSubText
                        )
                    }
                }

                // Total Dipakai
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Total Dipakai",
                            fontSize = 12.sp,
                            color = PinkSubText
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (totalKeluar >= 1_000_000)
                                "Rp ${String.format("%.2f", totalKeluar / 1_000_000f)} jt"
                            else if (totalKeluar >= 1000)
                                "Rp ${"%,d".format(totalKeluar / 1000).replace(",", ".")} rb"
                            else "Rp ${"%,d".format(totalKeluar).replace(",", ".")}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PinkText
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Dari $totalTrxKeluar transaksi",
                            fontSize = 11.sp,
                            color = PinkSubText
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Target Aktif
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Target Aktif",
                            fontSize = 12.sp,
                            color = PinkSubText
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "$totalAktif target",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PinkText
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Sedang berjalan",
                            fontSize = 11.sp,
                            color = PinkSubText
                        )
                    }
                }

                // Rata-rata/Bulan
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Rata-rata/Bulan",
                            fontSize = 12.sp,
                            color = PinkSubText
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (rataRata >= 1_000_000)
                                "Rp ${String.format("%.0f", rataRata / 1_000_000f)} jt"
                            else if (rataRata >= 1000)
                                "Rp ${"%,d".format(rataRata / 1000).replace(",", ".")} rb"
                            else "Rp ${"%,d".format(rataRata).replace(",", ".")}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PinkText
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "10 bulan terakhir",
                            fontSize = 11.sp,
                            color = PinkSubText
                        )
                    }
                }
            }

            // ── BAR CHART: Setor per Bulan ───────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Setor per Bulan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PinkText
                    )
                    Spacer(Modifier.height(16.dp))
                    if (list.isNotEmpty()) {
                        PinkBarChart(list = list)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Belum ada data",
                                fontSize = 13.sp,
                                color = PinkSubText
                            )
                        }
                    }
                }
            }

            // ── PROGRESS SEMUA TARGET ────────────────────────────
            if (list.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Progress Semua Target",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PinkText
                        )
                        Spacer(Modifier.height(16.dp))

                        list.forEachIndexed { i, cel ->
                            val p = if (cel.target > 0)
                                (cel.terkumpul.toFloat() / cel.target).coerceIn(0f, 1f) else 0f
                            val dotColor = progressColors[i % progressColors.size]

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Dot warna
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )

                                // Nama + bar
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        cel.nama,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PinkText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(5.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFFF5F5F5))
                                    ) {
                                        val animP by animateFloatAsState(
                                            p,
                                            tween(700, easing = EaseOutCubic),
                                            label = "sp$i"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(animP)
                                                .clip(RoundedCornerShape(50))
                                                .background(dotColor)
                                        )
                                    }
                                }

                                // Persentase
                                Text(
                                    "${(p * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dotColor
                                )
                            }

                            if (i < list.lastIndex) {
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🐷", fontSize = 36.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Belum ada celengan",
                                fontSize = 14.sp,
                                color = PinkSubText
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  AKTIVITAS SCREEN
// ═══════════════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════════════
//  AKTIVITAS SCREEN — Upgraded
// ═══════════════════════════════════════════════════════════════════

@Composable
fun AktivitasScreen(list: List<Celengan>) {
    var filterAktif by remember { mutableStateOf("Semua") }

    val semuaAktivitas = remember(list) {
        list.flatMap { cel ->
            cel.riwayat.map { trx -> Triple(cel.nama, trx, cel) }
        }.sortedByDescending { it.second.tanggal }
    }

    val today     = SimpleDateFormat("dd MMM yyyy", Locale("id")).format(Date())
    val yesterday = SimpleDateFormat("dd MMM yyyy", Locale("id"))
        .format(Date(System.currentTimeMillis() - 86_400_000L))

    val filtered = when (filterAktif) {
        "Setor" -> semuaAktivitas.filter { it.second.tipe == "MASUK" }
        "Pakai" -> semuaAktivitas.filter { it.second.tipe == "KELUAR" }
        else    -> semuaAktivitas
    }

    val grouped = filtered.groupBy { (_, trx, _) ->
        trx.tanggal.substringBefore(" •").trim()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F4))
    ) {

        // ── HEADER ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(Color(0xFFF8BBD0))
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 20.dp)
        ) {
            Column {
                Text(
                    "Aktivitas",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A148C)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Riwayat semua transaksi",
                    fontSize = 13.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }

        // ── FILTER CHIPS ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 14.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Semua", "Setor", "Pakai").forEach { f ->
                val isActive = filterAktif == f
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            if (isActive) Color(0xFFE91E8C) else Color.White
                        )
                        .border(
                            1.dp,
                            if (isActive) Color(0xFFE91E8C) else Color(0xFFE0C8D4),
                            RoundedCornerShape(50.dp)
                        )
                        .clickable { filterAktif = f }
                        .padding(horizontal = 22.dp, vertical = 9.dp)
                ) {
                    Text(
                        f,
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isActive) Color.White else Color(0xFF9E9E9E)
                    )
                }
            }
        }

        // ── ISI LIST ──────────────────────────────────────────────
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔔", fontSize = 40.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Belum ada aktivitas",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PinkSubText
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Mulai menabung untuk melihat aktivitas",
                        fontSize = 13.sp,
                        color = PinkSubText
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = 4.dp, bottom = 32.dp
                )
            ) {
                grouped.forEach { (dateKey, items) ->

                    // Label tanggal
                    item {
                        val dateLabel = when (dateKey) {
                            today     -> "Hari Ini"
                            yesterday -> "Kemarin"
                            else      -> dateKey
                        }
                        Text(
                            dateLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE91E8C),
                            modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)
                        )
                    }

                    // Kartu transaksi
                    items(items) { (namaCelengan, trx, _) ->
                        AktivitasCardBaru(
                            namaCelengan = namaCelengan,
                            trx          = trx
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AktivitasCardBaru(namaCelengan: String, trx: Transaksi) {
    val isMasuk  = trx.tipe == "MASUK"
    val timePart = if (trx.tanggal.contains("•"))
        trx.tanggal.substringAfter("•").trim().replace(":", ".") else ""

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isMasuk) Color(0xFFD6F5E3) else Color(0xFFFFE0EA)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isMasuk) "↑" else "↓",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMasuk) Color(0xFF43A047) else Color(0xFFE91E8C)
                )
            }

            // ── Info tengah ───────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${if (isMasuk) "Setor" else "Pakai"} Tabungan · $namaCelengan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF37474F),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (timePart.isNotEmpty()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        timePart,
                        fontSize = 12.sp,
                        color = Color(0xFFAAAAAA)
                    )
                }
            }

            // ── Nominal ───────────────────────────────────────────
            Text(
                "${if (isMasuk) "+" else "−"}Rp ${"%,d".format(trx.nominal).replace(",", ".")}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMasuk) Color(0xFF43A047) else Color(0xFFE91E8C)
            )
        }
    }
}

// ── Helper: chip ringkasan header ─────────────────────────────────
@Composable
fun AktSummaryChip(
    label: String,
    value: String,
    isGreen: Boolean?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(White.copy(0.16f))
            .padding(vertical = 10.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label.uppercase(),
                fontSize = 9.sp,
                color = White.copy(0.72f),
                letterSpacing = 0.3.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGreen == true) Color(0xFFA5D6A7) else White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Helper: filter pill ────────────────────────────────────────────
@Composable
fun AktFilterPill(text: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) Blue500 else White)
            .border(
                if (!isActive) 0.5.dp else 0.dp,
                Color(0xFFDDE3EE),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isActive) White else Color(0xFF546E7A)
        )
    }
}

// ── Helper: kartu satu transaksi ──────────────────────────────────
@Composable
fun AktivitasCard(namaCelengan: String, trx: Transaksi) {
    val isMasuk = trx.tipe == "MASUK"
    val datePart = trx.tanggal.substringBefore(" •").trim()
    val timePart = if (trx.tanggal.contains("•"))
        trx.tanggal.substringAfter("•").trim() else ""

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, Color(0xFFDDE3EE)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isMasuk) Color(0xFFE3F2FD) else Color(0xFFFFEBEE)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isMasuk) "🪙" else "💸", fontSize = 18.sp)
            }

            // Info tengah
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isMasuk) "Menabung berhasil" else "Pakai tabungan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A2E42),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (trx.keterangan.isNotEmpty()) {
                    Text(
                        trx.keterangan,
                        fontSize = 11.sp,
                        color = Color(0xFF546E7A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    "$datePart${if (timePart.isNotEmpty()) " • $timePart" else ""}",
                    fontSize = 11.sp,
                    color = TextHint
                )
            }

            // Nominal + badge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (isMasuk) "+" else "-"}Rp ${"%,d".format(trx.nominal)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMasuk) Color(0xFF43A047) else Color(0xFFEF5350)
                )
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isMasuk) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (isMasuk) "MASUK" else "KELUAR",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMasuk) Color(0xFF388E3C) else Color(0xFFC62828)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  HOME SCREEN
// ═══════════════════════════════════════════════════════════════════

@Composable
fun HomeScreen(
    list: List<Celengan>,
    onTambah: () -> Unit,
    onProfil: () -> Unit,
    onClickItem: (Celengan) -> Unit
) {
    var showNotification by remember {
        mutableStateOf(false)
    }
    var selectedTab by remember { mutableStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val totalTarget = list.sumOf { it.target }
    val totalTerkumpul = list.sumOf { it.terkumpul }
    val overallProgress =
        if (totalTarget > 0) (totalTerkumpul.toFloat() / totalTarget).coerceIn(0f, 1f) else 0f
    val totalTercapai = list.count { it.terkumpul >= it.target }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onTambah,
                containerColor = Blue500,
                contentColor = White,
                shape = CircleShape,
                modifier = Modifier.size(58.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(26.dp))
            }
        },
        containerColor = BgPage
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── HEADER ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = HeaderGrad,
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .padding(horizontal = 20.dp)
                    .padding(top = 48.dp, bottom = 24.dp)
            ) {
                Column {
                    // App bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(White.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_wishpay),
                                    contentDescription = "wishPay",
                                    modifier = Modifier.size(30.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    "wishPay",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                                Text(
                                    "Halo, semangat nabung!",
                                    fontSize = 11.sp,
                                    color = White.copy(0.72f)
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            // Tombol Profil — baru
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(White.copy(alpha = 0.18f))
                                    .clickable { onProfil() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👤", fontSize = 17.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Badge(
                                text = "${list.size} Aktif",
                                bgColor = White.copy(alpha = 0.20f),
                                textColor = White
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            val auth = FirebaseAuth.getInstance()
                            val context = LocalContext.current

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(White.copy(alpha = 0.2f))
                                    .clickable {
                                        showLogoutDialog = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Logout", color = White, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Summary cards — ringkas & rapi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        // Terkumpul
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(White.copy(alpha = 0.15f))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text("Terkumpul", fontSize = 11.sp, color = White.copy(0.75f))
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Rp ${"%,d".format(totalTerkumpul)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                            }
                        }

                        // Target
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(White.copy(alpha = 0.15f))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text("Target Total", fontSize = 11.sp, color = White.copy(0.75f))
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Rp ${"%,d".format(totalTarget)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                            }
                        }

                        // ✅ CARD BARU: Tercapai
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(White.copy(alpha = 0.15f))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text("Tercapai", fontSize = 11.sp, color = White.copy(0.75f))
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "$totalTercapai",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                                Text(
                                    "celengan",
                                    fontSize = 10.sp,
                                    color = White.copy(0.7f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Progress bar ringkas di header
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Progress keseluruhan",
                                fontSize = 11.sp,
                                color = White.copy(0.75f)
                            )
                            Text(
                                "${(overallProgress * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(White.copy(0.22f))
                        ) {
                            val animProg by animateFloatAsState(
                                overallProgress,
                                tween(700, easing = EaseOutCubic),
                                label = "hprog"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animProg)
                                    .clip(RoundedCornerShape(50))
                                    .background(White.copy(0.9f))
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── TAB BAR ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgSubtle)
                    .padding(3.dp)
            ) {
                listOf("Berlangsung", "Tercapai").forEachIndexed { idx, label ->
                    val selected = selectedTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) White else Color.Transparent)
                            .clickable { selectedTab = idx }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) Blue600 else TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── LIST ──────────────────────────────────────────────────
            val filteredList = when (selectedTab) {
                0 -> list.filter { it.terkumpul < it.target }
                else -> list.filter { it.terkumpul >= it.target }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgPage),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(BgSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_wishpay),
                                contentDescription = null,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            if (selectedTab == 0) "Belum ada tabungan" else "Belum ada yang tercapai",
                            fontSize = 15.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Tap tombol + untuk mulai menabung",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgPage),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { item ->
                        CelenganCard(item = item, onClick = { onClickItem(item) })
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // ═══════════════════════════════════════
    //  POPUP LOGOUT — Cute Bear Animated
    // ═══════════════════════════════════════
    if (showLogoutDialog) {

        val infiniteL = rememberInfiniteTransition(label = "logoutAnim")

        val bearBob by infiniteL.animateFloat(
            initialValue = 0f, targetValue = -10f,
            animationSpec = infiniteRepeatable(
                tween(1800, easing = EaseInOutSine),
                RepeatMode.Reverse
            ),
            label = "bearBobLogout"
        )
        val ringAlpha by infiniteL.animateFloat(
            initialValue = 0.25f, targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                tween(1000, easing = EaseInOutSine),
                RepeatMode.Reverse
            ),
            label = "ringAlphaLogout"
        )
        val heartBeat by infiniteL.animateFloat(
            initialValue = 1f, targetValue = 1.22f,
            animationSpec = infiniteRepeatable(
                tween(700, easing = EaseInOutSine),
                RepeatMode.Reverse
            ),
            label = "heartBeat"
        )
        val starAlpha1 by infiniteL.animateFloat(
            initialValue = 0.4f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(900, easing = EaseInOutSine),
                RepeatMode.Reverse
            ),
            label = "star1"
        )
        val starAlpha2 by infiniteL.animateFloat(
            initialValue = 0.4f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(
                    700,
                    delayMillis = 300,
                    easing = EaseInOutSine
                ), RepeatMode.Reverse
            ),
            label = "star2"
        )
        val starAlpha3 by infiniteL.animateFloat(
            initialValue = 0.4f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(
                    1100,
                    delayMillis = 150,
                    easing = EaseInOutSine
                ), RepeatMode.Reverse
            ),
            label = "star3"
        )
        val popupScale by animateFloatAsState(
            targetValue = if (showLogoutDialog) 1f else 0.85f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
            label = "popupScale"
        )

        val auth2 = FirebaseAuth.getInstance()
        val context2 = LocalContext.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A1628).copy(alpha = 0.72f))
                .clickable { showLogoutDialog = false },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .scale(popupScale)
                    .shadow(
                        elevation = 32.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = Blue500.copy(0.2f),
                        spotColor = Blue700.copy(0.25f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(White)
                    .border(2.dp, Blue100, RoundedCornerShape(28.dp))
                    .clickable(enabled = false) {}
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // Garis biru shimmer di atas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .background(BtnGrad)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 22.dp)
                    ) {

                        // ── Beruang bobble + bintang melayang ──────────
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Ring pulse luar
                            Box(
                                modifier = Modifier
                                    .size(118.dp)
                                    .clip(CircleShape)
                                    .border(
                                        1.5.dp,
                                        Blue300.copy(alpha = ringAlpha * 0.35f),
                                        CircleShape
                                    )
                            )
                            // Ring tengah
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .border(
                                        1.5.dp,
                                        Blue100.copy(alpha = ringAlpha * 0.5f),
                                        CircleShape
                                    )
                            )
                            // Lingkaran bg
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(Blue50)
                            )
                            // Beruang bobble
                            Text(
                                "🐻",
                                fontSize = 42.sp,
                                modifier = Modifier.offset(y = bearBob.dp)
                            )
                            // Bintang melayang
                            Text(
                                "⭐",
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 10.dp)
                                    .alpha(starAlpha1)
                            )
                            Text(
                                "✨",
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(x = 8.dp, y = (-8).dp)
                                    .alpha(starAlpha2)
                            )
                            Text(
                                "💫",
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset(x = 10.dp, y = 14.dp)
                                    .alpha(starAlpha3)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            "Mau pergi dulu? 🥺",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blue700,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Beruang akan kangen nunggu kamu balik~",
                            fontSize = 12.sp,
                            color = Blue400,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))

                        // ── Info box hati berdetak ─────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Blue50)
                                .border(1.5.dp, Blue100, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "💙",
                                fontSize = 20.sp,
                                modifier = Modifier.scale(heartBeat)
                            )
                            Column {
                                Text(
                                    "Tabunganmu tetap aman!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Blue700
                                )
                                Text(
                                    "Data kamu tersimpan rapi, nggak kemana-mana.",
                                    fontSize = 11.sp,
                                    color = Blue500,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // ── Status chip ────────────────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BgInput)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🪙", fontSize = 15.sp)
                                Text(
                                    "Status tabungan",
                                    fontSize = 12.sp,
                                    color = Blue600,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Blue500)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "Aman tersimpan",
                                    fontSize = 11.sp,
                                    color = White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── Tombol ─────────────────────────────────────
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                            // Batal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Blue50)
                                    .border(1.5.dp, Blue100, RoundedCornerShape(14.dp))
                                    .clickable { showLogoutDialog = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text("🌙", fontSize = 14.sp)
                                    Text(
                                        "Batal",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Blue600
                                    )
                                }
                            }

                            // Ya, Dadah!
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(50.dp)
                                    .shadow(
                                        10.dp, RoundedCornerShape(14.dp),
                                        ambientColor = Blue500.copy(0.35f),
                                        spotColor = Blue700.copy(0.4f)
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(BtnGrad)
                                    .clickable {
                                        showLogoutDialog = false
                                        val userId = auth2.currentUser?.uid ?: ""
                                        cancelNotification(context2, userId)
                                        auth2.signOut()
                                        (context2 as? ComponentActivity)?.setContent {
                                            App()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("👋", fontSize = 15.sp)
                                    Text(
                                        "Ya, Dadah!",
                                        color = White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  PINK STAT CARD — untuk home screen baru
// ═══════════════════════════════════════════════════════════════════
@Composable
fun PinkStatCard(
    modifier: Modifier = Modifier,
    iconBgColor: Color,
    label: String,
    value: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Kotak warna besar (seperti di HTML & foto 1)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(iconBgColor)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                label,
                fontSize = 11.sp,
                color = PinkSubText,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PinkText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  PINK CELENGAN CARD — untuk home screen baru
// ═══════════════════════════════════════════════════════════════════
@Composable
fun PinkCelenganCard(item: Celengan, index: Int, onClick: () -> Unit) {
    val progress = if (item.target > 0)
        (item.terkumpul.toFloat() / item.target).coerceIn(0f, 1f) else 0f

    val iconColors = listOf(
        Color(0xFFFFE0F0), // pink
        Color(0xFFEDE0FF), // purple
        Color(0xFFFFF3D6), // amber
        Color(0xFFC8F0E0), // mint
        Color(0xFFE0F0FF)  // blue
    )
    val iconEmojis = listOf("👗", "✈️", "📱", "🏠", "🎯")
    val progressColors = listOf(
        Color(0xFFD060A0), // pink
        Color(0xFF8060C0), // purple
        Color(0xFFB07820), // amber
        Color(0xFF208860), // green
        Color(0xFF4080C0)  // blue
    )

    val iconBg    = iconColors[index % iconColors.size]
    val barColor  = progressColors[index % progressColors.size]
    val emoji     = iconEmojis[index % iconEmojis.size]

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.5.dp, Color(0xFFF2C8E0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon bulat/rounded dengan emoji
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.nama,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6E2050),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${(progress * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Rp ${"%,d".format(item.terkumpul).replace(",",".")} dari Rp ${"%,d".format(item.target).replace(",",".")}",
                    fontSize = 11.sp,
                    color = Color(0xFFA06080)
                )
                Spacer(Modifier.height(8.dp))
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFF5EEF4))
                ) {
                    val animP by animateFloatAsState(
                        progress, tween(700, easing = EaseOutCubic), label = "pink_p"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animP)
                            .clip(RoundedCornerShape(50))
                            .background(barColor)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  CELENGAN CARD — bersih & minimalis
// ═══════════════════════════════════════════════════════════════════

@Composable
fun CelenganCard(item: Celengan, onClick: () -> Unit) {
    val progress = if (item.target > 0)
        (item.terkumpul.toFloat() / item.target).coerceIn(0f, 1f) else 0f
    val tercapai = item.terkumpul >= item.target

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            Brush.linearGradient(listOf(Color(0xFFE0D7FF), Color(0xFFF0EDFF)))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Foto impian (jika ada)
            if (!item.image.isNullOrEmpty()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(Uri.parse(item.image)),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Baris atas: icon + nama + badge kategori
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (tercapai) Color(0xFFE8F5E9) else Blue50),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (tercapai) "🏆" else "🐷", fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            item.nama,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "Target: ${
                                if (item.target >= 1_000_000)
                                    "Rp${String.format("%.0f", item.target / 1_000_000f)}jt"
                                else "Rp${"%,d".format(item.target)}"
                            }",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                // Badge kategori / status
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (tercapai) Color(0xFFE8F5E9) else Blue50)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (tercapai) "TERCAPAI" else item.jenis.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tercapai) GreenSoft else Blue600
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Label progress
            Text(
                "Progres Tabungan",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(Modifier.height(6.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFEEF2F8))
            ) {
                val animProg by animateFloatAsState(
                    progress, tween(700, easing = EaseOutCubic), label = "cprog"
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animProg)
                        .clip(RoundedCornerShape(50))
                        .background(if (tercapai) GreenSoft else Blue600)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Nominal row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Rp ${"%,d".format(item.terkumpul)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    "Rp ${"%,d".format(item.target)}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            // Tanggal tercapai (jika sudah tercapai)
            if (tercapai && item.riwayat.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFEEF2F8))
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday, null,
                            tint = GreenSoft, modifier = Modifier.size(12.dp)
                        )
                        Text(
                            "Tercapai pada ${item.riwayat.lastOrNull()?.tanggal ?: ""}",
                            fontSize = 11.sp,
                            color = GreenSoft
                        )
                    }
                    Text(
                        "Detail",
                        fontSize = 12.sp,
                        color = Blue600,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else if (!tercapai) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blue600,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun CoinItem(index: Int, randomX: Int) {
    val offsetY = remember { Animatable(-120f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 80L)
        offsetY.animateTo(
            targetValue = 1200f,
            animationSpec = tween(2200, easing = EaseInCubic)
        )
    }
    Text(
        text = "🪙",
        fontSize = (18 + (index % 4) * 3).sp,
        modifier = Modifier
            .offset(x = randomX.dp, y = offsetY.value.dp)
            .alpha(0.92f)
    )
}

// ═══════════════════════════════════════════════════════════════════
//  DETAIL SCREEN
// ═══════════════════════════════════════════════════════════════════

@Composable
fun DetailScreen(
    celengan: Celengan,
    onKembali: () -> Unit,
    onDelete: (Celengan) -> Unit,
    onUpdate: () -> Unit,
    onEdit: () -> Unit
) {
    var inputSetor    by remember { mutableStateOf("") }
    var inputPakai    by remember { mutableStateOf("") }
    var showSetorSheet by remember { mutableStateOf(false) }
    var showPakaiSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSuksesDialog by remember { mutableStateOf(false) }
    var nominalSukses  by remember { mutableStateOf(0) }
    var showTercapaiDialog by remember { mutableStateOf(false) }
    var showMenuSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    val progress = if (celengan.target > 0)
        (celengan.terkumpul.toFloat() / celengan.target).coerceIn(0f, 1f) else 0f
    val sisa = (celengan.target - celengan.terkumpul).coerceAtLeast(0)

    // Hitung streak (hari berturut-turut ada transaksi MASUK)
    val streak = run {
        val sdf  = SimpleDateFormat("dd MMM yyyy", Locale("id"))
        val days = celengan.riwayat
            .filter { it.tipe == "MASUK" }
            .mapNotNull { runCatching { sdf.parse(it.tanggal.substringBefore(" •")) }.getOrNull() }
            .map { it.time / 86_400_000L }
            .toSortedSet()
            .toList()
            .asReversed()
        var count = 0
        var prev  = System.currentTimeMillis() / 86_400_000L
        for (d in days) {
            if (prev - d <= 1L) { count++; prev = d } else break
        }
        count
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F4))
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── HEADER PINK ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(Color(0xFFF8BBD0))
                    .padding(horizontal = 20.dp)
                    .padding(top = 52.dp, bottom = 24.dp)
            ) {
                // Tombol back kiri
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.7f))
                        .clickable { onKembali() }
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ArrowBack, null,
                        tint = Color(0xFF4A148C),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Tombol 3-dot kanan — dengan dropdown menu
                var showDropdown by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.7f))
                            .clickable { showDropdown = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MoreVert, null,
                            tint = Color(0xFF4A148C),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Bottom sheet menu — muncul di atas semua konten
                    if (showDropdown) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                                .clickable { showDropdown = false },
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Card(
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = false) {}
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                        .padding(top = 16.dp, bottom = 32.dp)
                                ) {
                                    // Drag handle
                                    Box(
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFFE8D0DC))
                                            .align(Alignment.CenterHorizontally)
                                    )

                                    Spacer(Modifier.height(16.dp))

                                    // Nama celengan sebagai judul sheet
                                    Text(
                                        celengan.nama,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF4A148C),
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    // ── Edit Celengan ──────────────────────────────
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                showDropdown = false
                                                onEdit()
                                            }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0xFFF3E5F5)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Edit, null,
                                                tint = Color(0xFF9C27B0),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                "Edit Celengan",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF37474F)
                                            )
                                            Text(
                                                "Ubah nama, target, atau pengingat",
                                                fontSize = 12.sp,
                                                color = Color(0xFF9E9E9E)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(4.dp))

                                    // ── Hapus Celengan ─────────────────────────────
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                showDropdown = false
                                                showDeleteDialog = true
                                            }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0xFFFFE8EC)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Delete, null,
                                                tint = Color(0xFFE91E8C),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                "Hapus Celengan",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFFE91E8C)
                                            )
                                            Text(
                                                "Hapus celengan dan semua datanya",
                                                fontSize = 12.sp,
                                                color = Color(0xFF9E9E9E)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Konten tengah
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon celengan
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🐷", fontSize = 28.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Celengan",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                    Text(
                        celengan.nama,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A148C),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ── SCROLLABLE CONTENT ───────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ── CARD TOTAL TERKUMPUL + PROGRESS ──────────────────
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(1.dp, Color(0xFFF2C8E0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Total Terkumpul",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Rp ${"%,d".format(celengan.terkumpul).replace(",", ".")}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE91E8C)
                        )
                        Text(
                            "dari target Rp ${"%,d".format(celengan.target).replace(",", ".")}",
                            fontSize = 13.sp,
                            color = Color(0xFF9E9E9E)
                        )
                        Spacer(Modifier.height(16.dp))

                        // Progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFFFE0EA))
                        ) {
                            val animP by animateFloatAsState(
                                progress, tween(700, easing = EaseOutCubic), label = "dp"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animP)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFE91E8C), Color(0xFF9C27B0))
                                        )
                                    )
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${(progress * 100).toInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE91E8C)
                            )
                            Text(
                                "Sisa Rp ${"%,d".format(sisa).replace(",", ".")}",
                                fontSize = 12.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                }

                // ── DUA TOMBOL AKSI ───────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tombol + Setor (filled pink-purple)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFE91E8C), Color(0xFF9C27B0))
                                )
                            )
                            .clickable { showSetorSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("↑", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                    color = Color.White)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "+ Setor",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Tombol Pakai (outline)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .border(
                                1.5.dp,
                                Color(0xFFF2C8E0),
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { showPakaiSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFE0EA)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("↓", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE91E8C))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Pakai",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE91E8C)
                            )
                        }
                    }
                }

                // ── 3 INFO CHIPS ──────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Frekuensi
                    DetailInfoChip(
                        modifier = Modifier.weight(1f),
                        value = celengan.jenis,
                        label = "FREKUENSI"
                    )
                    // Target Tanggal
                    DetailInfoChip(
                        modifier = Modifier.weight(1f),
                        value = "—",
                        label = "TARGET TANGGAL"
                    )
                    // Streak Nabung
                    DetailInfoChip(
                        modifier = Modifier.weight(1f),
                        value = "$streak hari",
                        label = "STREAK NABUNG"
                    )
                }

                // ── RIWAYAT TRANSAKSI ─────────────────────────────────
                if (celengan.riwayat.isNotEmpty()) {
                    Text(
                        "Riwayat Transaksi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        celengan.riwayat.reversed().forEach { trx ->
                            DetailTrxCard(trx = trx)
                        }
                    }
                }
            }
        }

        // ── BOTTOM SHEET: SETOR ───────────────────────────────────────────
        if (showSetorSheet) {
            var catatanSetor by remember { mutableStateOf("") }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { showSetorSheet = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 12.dp, bottom = 32.dp)
                    ) {
                        // Drag handle
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFE0E0E0))
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.height(20.dp))

                        // Judul + subtitle
                        Text(
                            "Setor Tabungan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF37474F)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tambahkan uang ke celengan ${celengan.nama}",
                            fontSize = 13.sp,
                            color = Color(0xFF9E9E9E)
                        )
                        Spacer(Modifier.height(20.dp))

                        // Input nominal besar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFFF0F4))
                                .border(1.5.dp, Color(0xFFF2C8E0), RoundedCornerShape(14.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Rp ",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE91E8C)
                                )
                                BasicTextField(
                                    value = if (inputSetor == "0" || inputSetor.isEmpty()) "" else inputSetor,
                                    onValueChange = { inputSetor = if (it.isEmpty()) "0" else it },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF37474F)
                                    ),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    decorationBox = { inner ->
                                        if (inputSetor.isEmpty() || inputSetor == "0") {
                                            Text(
                                                "0",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFBBBBBB)
                                            )
                                        }
                                        inner()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Chip nominal — 2 baris, 4 + 2
                        val nominalsSetor = listOf(10000, 25000, 50000, 100000, 200000, 500000)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            nominalsSetor.chunked(4).forEach { rowItems ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { v ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(
                                                    if (inputSetor == v.toString())
                                                        Color(0xFFE91E8C)
                                                    else Color(0xFFFFF0F4)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (inputSetor == v.toString())
                                                        Color(0xFFE91E8C)
                                                    else Color(0xFFF2C8E0),
                                                    RoundedCornerShape(20.dp)
                                                )
                                                .clickable { inputSetor = v.toString() }
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                "%,d".format(v).replace(",", "."),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (inputSetor == v.toString())
                                                    Color.White
                                                else Color(0xFFE91E8C)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Catatan opsional
                        OutlinedTextField(
                            value = catatanSetor,
                            onValueChange = { catatanSetor = it },
                            placeholder = {
                                Text(
                                    "Catatan (opsional)...",
                                    color = Color(0xFFBBBBBB),
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF2C8E0),
                                unfocusedBorderColor = Color(0xFFF2C8E0),
                                focusedContainerColor = Color(0xFFFFF0F4),
                                unfocusedContainerColor = Color(0xFFFFF0F4),
                                cursorColor = Color(0xFFE91E8C)
                            ),
                            singleLine = true
                        )

                        Spacer(Modifier.height(20.dp))

                        // Tombol Setor Sekarang
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFE91E8C), Color(0xFF9C27B0))
                                    )
                                )
                                .clickable {
                                    val tambah = inputSetor.replace(".", "").toIntOrNull() ?: 0
                                    if (tambah > 0) {
                                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                        celengan.terkumpul += tambah
                                        val now = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()).format(Date())
                                        celengan.riwayat.add(Transaksi(tanggal = now, nominal = tambah, tipe = "MASUK", keterangan = catatanSetor))
                                        onUpdate()
                                        nominalSukses = tambah
                                        inputSetor = ""
                                        showSetorSheet = false
                                        showSuksesDialog = true
                                        if (celengan.terkumpul >= celengan.target) showTercapaiDialog = true
                                        if (userId.isNotEmpty() && celengan.id.isNotEmpty()) {
                                            scope.launch {
                                                try {
                                                    FirestoreManager.tambahSaldo(userId, celengan.id, tambah)
                                                    FirestoreManager.tambahRiwayat(userId, celengan.id, Transaksi(now, tambah, "MASUK", catatanSetor))
                                                } catch (e: Exception) { e.printStackTrace() }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Setor Sekarang",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // ── BOTTOM SHEET: PAKAI ───────────────────────────────────────────
        if (showPakaiSheet) {
            var catatanPakai by remember { mutableStateOf("") }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { showPakaiSheet = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 12.dp, bottom = 32.dp)
                    ) {
                        // Drag handle
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFE0E0E0))
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.height(20.dp))

                        // Judul + subtitle
                        Text(
                            "Pakai Tabungan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF37474F)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Gunakan tabungan dari celengan ${celengan.nama}",
                            fontSize = 13.sp,
                            color = Color(0xFF9E9E9E)
                        )
                        Spacer(Modifier.height(20.dp))

                        // Input nominal besar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFFF0F4))
                                .border(1.5.dp, Color(0xFFF2C8E0), RoundedCornerShape(14.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Rp ",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE91E8C)
                                )
                                BasicTextField(
                                    value = if (inputPakai == "0" || inputPakai.isEmpty()) "" else inputPakai,
                                    onValueChange = { inputPakai = if (it.isEmpty()) "0" else it },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF37474F)
                                    ),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    decorationBox = { inner ->
                                        if (inputPakai.isEmpty() || inputPakai == "0") {
                                            Text(
                                                "0",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFBBBBBB)
                                            )
                                        }
                                        inner()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Chip nominal — 2 baris, 4 + 2
                        val nominalsPakai = listOf(10000, 25000, 50000, 100000, 200000, 500000)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            nominalsPakai.chunked(4).forEach { rowItems ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { v ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(
                                                    if (inputPakai == v.toString())
                                                        Color(0xFFE91E8C)
                                                    else Color(0xFFFFF0F4)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (inputPakai == v.toString())
                                                        Color(0xFFE91E8C)
                                                    else Color(0xFFF2C8E0),
                                                    RoundedCornerShape(20.dp)
                                                )
                                                .clickable { inputPakai = v.toString() }
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                "%,d".format(v).replace(",", "."),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (inputPakai == v.toString())
                                                    Color.White
                                                else Color(0xFFE91E8C)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Catatan opsional
                        OutlinedTextField(
                            value = catatanPakai,
                            onValueChange = { catatanPakai = it },
                            placeholder = {
                                Text(
                                    "Catatan (opsional)...",
                                    color = Color(0xFFBBBBBB),
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF2C8E0),
                                unfocusedBorderColor = Color(0xFFF2C8E0),
                                focusedContainerColor = Color(0xFFFFF0F4),
                                unfocusedContainerColor = Color(0xFFFFF0F4),
                                cursorColor = Color(0xFFE91E8C)
                            ),
                            singleLine = true
                        )

                        Spacer(Modifier.height(20.dp))

                        // Tombol Pakai Tabungan
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFE91E8C))
                                .clickable {
                                    val kurang = inputPakai.replace(".", "").toIntOrNull() ?: 0
                                    if (kurang > 0 && celengan.terkumpul >= kurang) {
                                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                        celengan.terkumpul -= kurang
                                        val now = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()).format(Date())
                                        val trxK = Transaksi(now, kurang, "KELUAR", catatanPakai)
                                        celengan.riwayat.add(trxK)
                                        onUpdate()
                                        inputPakai = ""
                                        showPakaiSheet = false
                                        if (userId.isNotEmpty() && celengan.id.isNotEmpty()) {
                                            scope.launch {
                                                try {
                                                    FirestoreManager.kurangiSaldo(userId, celengan.id, kurang)
                                                    FirestoreManager.tambahRiwayat(userId, celengan.id, trxK)
                                                } catch (e: Exception) { e.printStackTrace() }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Pakai Tabungan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // ── POPUP DELETE ──────────────────────────────────────────────
        if (showDeleteDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { showDeleteDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🗑️", fontSize = 40.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Hapus Tabungan?", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = Color(0xFF37474F))
                        Spacer(Modifier.height(6.dp))
                        Text("Data tidak bisa dikembalikan.",
                            fontSize = 13.sp, color = Color(0xFF9E9E9E),
                            textAlign = TextAlign.Center)
                        Spacer(Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f).height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF5F5F5))
                                    .clickable { showDeleteDialog = false },
                                contentAlignment = Alignment.Center
                            ) { Text("Batal", fontWeight = FontWeight.SemiBold, color = Color(0xFF9E9E9E)) }
                            Box(
                                modifier = Modifier
                                    .weight(1f).height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEF5350))
                                    .clickable { onDelete(celengan) },
                                contentAlignment = Alignment.Center
                            ) { Text("Hapus", fontWeight = FontWeight.Bold, color = Color.White) }
                        }
                    }
                }
            }
        }

        // ── POPUP SUKSES SETOR ────────────────────────────────────────
        if (showSuksesDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showSuksesDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉", fontSize = 44.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Nabung Berhasil!", fontSize = 18.sp,
                            fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "+Rp ${"%,d".format(nominalSukses).replace(",", ".")}",
                            fontSize = 28.sp, fontWeight = FontWeight.Bold,
                            color = Color(0xFFE91E8C)
                        )
                        Spacer(Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth().height(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFFE91E8C), Color(0xFF9C27B0))))
                                .clickable { showSuksesDialog = false },
                            contentAlignment = Alignment.Center
                        ) { Text("Sip, Lanjut!", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }

        // ── POPUP TERCAPAI ────────────────────────────────────────────
        if (showTercapaiDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showTercapaiDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏆", fontSize = 44.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Target Tercapai!", fontSize = 18.sp,
                            fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                        Spacer(Modifier.height(6.dp))
                        Text("Selamat! Impianmu sudah di depan mata!",
                            fontSize = 13.sp, color = Color(0xFF9E9E9E),
                            textAlign = TextAlign.Center)
                        Spacer(Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth().height(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFFE91E8C), Color(0xFF9C27B0))))
                                .clickable { showTercapaiDialog = false },
                            contentAlignment = Alignment.Center
                        ) { Text("Yeay!", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

// ── Info chip untuk detail screen ─────────────────────────────────
@Composable
fun DetailInfoChip(modifier: Modifier = Modifier, value: String, label: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color(0xFFF2C8E0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE91E8C),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                label,
                fontSize = 9.sp,
                color = Color(0xFF9E9E9E),
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Kartu transaksi untuk detail screen ───────────────────────────
@Composable
fun DetailTrxCard(trx: Transaksi) {
    val isMasuk  = trx.tipe == "MASUK"
    val datePart = trx.tanggal.substringBefore(" •").trim()
    val timePart = if (trx.tanggal.contains("•"))
        trx.tanggal.substringAfter("•").trim().replace(":", ".") else ""

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color(0xFFF5EEF4)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isMasuk) Color(0xFFD6F5E3) else Color(0xFFFFE0EA)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isMasuk) "↑" else "↓",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMasuk) Color(0xFF43A047) else Color(0xFFE91E8C)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isMasuk) "Setor Tabungan" else "Pakai Tabungan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF37474F)
                )
                if (datePart.isNotEmpty() || timePart.isNotEmpty()) {
                    Text(
                        "${datePart}${if (timePart.isNotEmpty()) " · $timePart" else ""}",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
            Text(
                "${if (isMasuk) "+" else "−"}Rp ${"%,d".format(trx.nominal).replace(",", ".")}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMasuk) Color(0xFF43A047) else Color(0xFFE91E8C)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  TAMBAH SCREEN
// ═══════════════════════════════════════════════════════════════════

@Composable
fun TambahScreen(
    onSimpan: (Celengan) -> Unit,
    onKembali: () -> Unit
) {
    var nama         by remember { mutableStateOf("") }
    var target       by remember { mutableStateOf("") }
    var inputNabung  by remember { mutableStateOf("") }
    var tanggalTarget by remember { mutableStateOf("") }
    var jenis        by remember { mutableStateOf("Harian") }
    var imageUri     by remember { mutableStateOf<Uri?>(null) }
    var notifAktif   by remember { mutableStateOf(false) }
    var jam          by remember { mutableStateOf("09:00") }
    var jamInt       by remember { mutableStateOf(9) }
    var menitInt     by remember { mutableStateOf(0) }
    var hariTerpilih by remember { mutableStateOf(setOf<String>()) }
    var showTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { imageUri = saveImageToInternalStorage(context, it) } }

    val canSave = nama.isNotEmpty() && (target.toIntOrNull() ?: 0) > 0

    Scaffold(
        bottomBar = {
            WishPayBottomNav(
                currentNav   = "none",
                onNavChange  = { onKembali() },
                onTambah     = { /* sudah di sini */ },
                onProfil     = { onKembali() }
            )
        },
        containerColor = Color(0xFFFFF0F4)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFFF0F4))
        ) {
            // ── HEADER PINK ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Color(0xFFF8BBD0))
                    .padding(horizontal = 20.dp)
                    .padding(top = 52.dp, bottom = 20.dp)
            ) {
                Column {
                    Text(
                        "Tambah Celengan",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A148C)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Buat target tabungan baru",
                        fontSize = 13.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            // ── FORM SCROLLABLE ──────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ── FOTO PICKER ──────────────────────────────────────
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE8D5E8), RoundedCornerShape(16.dp))
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF000000).copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CameraAlt, null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Ganti Foto",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEDE7F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt, null,
                                    tint = Color(0xFF7E57C2),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                "Tambah Foto",
                                fontSize = 13.sp,
                                color = PinkMain,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "(Opsional)",
                                fontSize = 11.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                }

                // ── NAMA TABUNGAN ────────────────────────────────────
                PinkFormField(
                    label       = "Nama Tabungan",
                    value       = nama,
                    onValueChange = { nama = it },
                    placeholder = "Misal: Liburan ke Jepang"
                )

                // ── TARGET TABUNGAN ──────────────────────────────────
                PinkFormField(
                    label       = "Target Tabungan (Rp)",
                    value       = target,
                    onValueChange = { target = it },
                    placeholder = "Rp 0"
                )

                // ── FREKUENSI MENABUNG ───────────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Frekuensi Menabung",
                        fontSize = 13.sp,
                        color = Color(0xFF5C5C7A),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Harian", "Mingguan", "Bulanan").forEach { j ->
                            val dipilih = jenis == j
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(if (dipilih) PinkMain else Color.White)
                                    .border(
                                        if (!dipilih) 1.dp else 0.dp,
                                        Color(0xFFE0C0D0),
                                        RoundedCornerShape(50.dp)
                                    )
                                    .clickable { jenis = j },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    j,
                                    fontSize = 13.sp,
                                    fontWeight = if (dipilih) FontWeight.Bold else FontWeight.Normal,
                                    color = if (dipilih) Color.White else Color(0xFF9E9E9E)
                                )
                            }
                        }
                    }
                }

                // ── JUMLAH NABUNG HARI INI ────────────────────────────
                PinkFormField(
                    label       = "Jumlah Nabung Hari Ini",
                    value       = inputNabung,
                    onValueChange = { inputNabung = it },
                    placeholder = "Rp 0"
                )

                // ── TANGGAL TARGET ────────────────────────────────────
                PinkFormField(
                    label       = "Tanggal Target",
                    value       = tanggalTarget,
                    onValueChange = { tanggalTarget = it },
                    placeholder = "Pilih tanggal..."
                )

                // ── PENGINGAT MENABUNG ────────────────────────────────
                Card(
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(1.dp, Color(0xFFF0D8E8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFCE4EC)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Notifications, null,
                                        tint = PinkMain,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(
                                    "Pengingat Menabung",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF37474F)
                                )
                            }
                            Switch(
                                checked = notifAktif,
                                onCheckedChange = { notifAktif = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor  = Color.White,
                                    checkedTrackColor  = PinkMain,
                                    uncheckedThumbColor = Color(0xFFCDD5E0),
                                    uncheckedTrackColor = Color(0xFFEEF2F8)
                                )
                            )
                        }

                        AnimatedVisibility(visible = notifAktif) {
                            Column {
                                Spacer(Modifier.height(14.dp))
                                HorizontalDivider(color = Color(0xFFF5E0EA))
                                Spacer(Modifier.height(14.dp))

                                // Waktu pengingat
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Waktu Pengingat",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF37474F)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(Color(0xFFFCE4EC))
                                            .border(
                                                1.dp, Color(0xFFF48FB1),
                                                RoundedCornerShape(50.dp)
                                            )
                                            .clickable { showTimePicker = true }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                jam,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PinkMain
                                            )
                                            Icon(
                                                Icons.Default.Timer, null,
                                                tint = PinkMain,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(14.dp))

                                Text(
                                    "Ulangi Pada",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF37474F)
                                )
                                Spacer(Modifier.height(10.dp))

                                val singkatHari = listOf("S", "S", "R", "K", "J", "S", "M")
                                val fullHari    = listOf(
                                    "Senin", "Selasa", "Rabu",
                                    "Kamis", "Jumat", "Sabtu", "Minggu"
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    fullHari.forEachIndexed { i, hari ->
                                        val dipilih = hariTerpilih.contains(hari)
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (dipilih) PinkMain
                                                    else Color(0xFFF5F0F3)
                                                )
                                                .border(
                                                    if (!dipilih) 0.5.dp else 0.dp,
                                                    Color(0xFFE8D5E8),
                                                    CircleShape
                                                )
                                                .clickable {
                                                    hariTerpilih =
                                                        if (dipilih) hariTerpilih - hari
                                                        else hariTerpilih + hari
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                singkatHari[i],
                                                fontSize = 12.sp,
                                                fontWeight = if (dipilih)
                                                    FontWeight.Bold else FontWeight.Normal,
                                                color = if (dipilih) Color.White
                                                else Color(0xFF9E9E9E)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // ── TOMBOL SIMPAN ────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            if (canSave) 6.dp else 0.dp,
                            RoundedCornerShape(16.dp),
                            ambientColor = PinkMain.copy(alpha = 0.35f),
                            spotColor    = PinkDark.copy(alpha = 0.35f)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (canSave) PinkMain else Color(0xFFE0C8D4))
                        .clickable(enabled = canSave) {
                            val t      = target.toIntOrNull() ?: 0
                            val nabung = inputNabung.toIntOrNull() ?: 0
                            if (notifAktif && jam.isNotEmpty()) {
                                val userId =
                                    FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                scheduleNotification(context, jam, userId)
                            }
                            onSimpan(
                                Celengan(
                                    nama       = nama,
                                    target     = t,
                                    terkumpul  = nabung,
                                    image      = imageUri?.toString(),
                                    nominal    = 0,
                                    jenis      = jenis,
                                    notifAktif = notifAktif,
                                    jamNotif   = jam,
                                    hariNotif  = hariTerpilih.toList()
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Simpan Celengan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canSave) Color.White else Color(0xFF9E9E9E)
                    )
                }
            }
        }
    }

    // ── CUSTOM TIME PICKER (Image 1) ─────────────────────────────────
    if (showTimePicker) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000).copy(alpha = 0.4f))
                .clickable { showTimePicker = false },
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                shape  = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Atur Waktu Pengingat",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PinkMain
                    )
                    Spacer(Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ── JAM ──────────────────────────────────────
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFCE4EC))
                                    .clickable { jamInt = (jamInt + 1) % 24 },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("▲", fontSize = 16.sp, color = PinkMain,
                                    fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                String.format("%02d", jamInt),
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF37474F)
                            )
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFCE4EC))
                                    .clickable { jamInt = (jamInt - 1 + 24) % 24 },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("▼", fontSize = 16.sp, color = PinkMain,
                                    fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            ":",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF37474F),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // ── MENIT ─────────────────────────────────────
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFCE4EC))
                                    .clickable { menitInt = (menitInt + 5) % 60 },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("▲", fontSize = 16.sp, color = PinkMain,
                                    fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                String.format("%02d", menitInt),
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF37474F)
                            )
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFCE4EC))
                                    .clickable { menitInt = (menitInt - 5 + 60) % 60 },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("▼", fontSize = 16.sp, color = PinkMain,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PinkMain)
                            .clickable {
                                jam = String.format("%02d:%02d", jamInt, menitInt)
                                showTimePicker = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Simpan Waktu",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun PinkFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontSize = 13.sp,
            color = Color(0xFF5C5C7A),
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFFBBBBBB), fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PinkMain,
                unfocusedBorderColor = Color(0xFFE8D5E8),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = PinkMain,
                focusedTextColor = Color(0xFF37474F),
                unfocusedTextColor = Color(0xFF37474F)
            )
        )
    }
}

// ── Helper: label form ─────────────────────────────────────────────




// ═══════════════════════════════════════════════════════════════════
//  EDIT SCREEN
// ═══════════════════════════════════════════════════════════════════

@Composable
fun EditScreen(
    celengan: Celengan,
    onSimpan: () -> Unit,
    onKembali: () -> Unit
) {
    var nama by remember { mutableStateOf(celengan.nama) }
    var target by remember { mutableStateOf(celengan.target.toString()) }
    var nominal by remember { mutableStateOf(celengan.nominal.toString()) }
    var jenis by remember { mutableStateOf(celengan.jenis) }
    var imageUri by remember {
        mutableStateOf<Uri?>(
            if (!celengan.image.isNullOrEmpty()) Uri.parse(celengan.image) else null
        )
    }
    var notifAktif by remember { mutableStateOf(celengan.notifAktif) }
    var jam by remember { mutableStateOf(celengan.jamNotif) }
    var hariTerpilih by remember { mutableStateOf(celengan.hariNotif.toSet()) }
    var showSimpanDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { imageUri = saveImageToInternalStorage(context, it) }
    }

    // Animasi pulse untuk popup konfirmasi
    val infiniteE = rememberInfiniteTransition(label = "editAnim")
    val bearBobE by infiniteE.animateFloat(
        initialValue = 0f, targetValue = -8f,
        animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "bearBobEdit"
    )
    val ringAlphaE by infiniteE.animateFloat(
        initialValue = 0.25f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "ringAlphaEdit"
    )
    val popupScaleE by animateFloatAsState(
        targetValue = if (showSimpanDialog) 1f else 0.85f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
        label = "popupScaleEdit"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPage)
    ) {
        // ── HEADER ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = HeaderGrad,
                    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                )
                .padding(horizontal = 20.dp)
                .padding(top = 48.dp, bottom = 22.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(White.copy(0.18f))
                            .clickable { onKembali() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            null,
                            tint = White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            "Edit Celengan",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        Text("Ubah detail tabunganmu", fontSize = 11.sp, color = White.copy(0.72f))
                    }
                }
            }
        }

        // ── FORM ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Foto picker
            CleanCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(TextPrimary.copy(0.28f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(White.copy(0.88f)),
                                    contentAlignment = Alignment.Center
                                ) { Text("📷", fontSize = 20.sp) }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Ganti Foto",
                                    fontSize = 12.sp,
                                    color = White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Brush.linearGradient(listOf(Blue100, Blue50)))
                                    .border(
                                        width = 1.5.dp,
                                        brush = Brush.linearGradient(listOf(Blue300, Blue100)),
                                        shape = RoundedCornerShape(18.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    null,
                                    tint = Blue500,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Tambah Foto Impian",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Blue500
                            )
                            Text(
                                "Ketuk untuk memilih gambar",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Detail tabungan
            CleanCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Detail Tabungan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = nama,
                        onValueChange = { nama = it },
                        label = { Text("Nama Tabungan") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            focusedLabelColor = Blue500,
                            cursorColor = Blue500
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it },
                        label = { Text("Target Tabungan") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        prefix = { Text("Rp ", color = TextHint) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            focusedLabelColor = Blue500,
                            cursorColor = Blue500
                        )
                    )
                }
            }

            // Rencana pengisian
            CleanCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Rencana Pengisian",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        listOf("Harian", "Mingguan", "Bulanan").forEach { j ->
                            val dipilih = jenis == j
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (dipilih) Blue500 else Color.Transparent)
                                    .clickable { jenis = j }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    j,
                                    fontSize = 12.sp,
                                    fontWeight = if (dipilih) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (dipilih) White else TextSecondary
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = nominal,
                        onValueChange = { nominal = it },
                        label = { Text("Nominal $jenis") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        prefix = { Text("Rp ", color = TextHint) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            focusedLabelColor = Blue500,
                            cursorColor = Blue500
                        )
                    )
                }
            }

            // Notifikasi
            CleanCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Blue50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    null,
                                    tint = Blue500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Aktifkan Pengingat",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    "Jadwalkan waktu nabung",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = notifAktif,
                            onCheckedChange = { notifAktif = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Blue500
                            )
                        )
                    }
                    AnimatedVisibility(visible = notifAktif) {
                        Column {
                            Spacer(Modifier.height(14.dp))
                            HorizontalDivider(color = BgSubtle)
                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BgInput)
                                    .padding(14.dp)
                                    .clickable {
                                        val parts = jam.split(":")
                                        TimePickerDialog(context, { _, h, m ->
                                            jam = String.format("%02d:%02d", h, m)
                                        }, parts[0].toInt(), parts[1].toInt(), true).show()
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Waktu Pengingat", fontSize = 11.sp, color = TextSecondary)
                                    Text(
                                        jam,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Blue500
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Blue50),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        null,
                                        tint = Blue500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "Hari Pengingat",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(10.dp))
                            val listHari = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                            val fullHari = listOf(
                                "Minggu",
                                "Senin",
                                "Selasa",
                                "Rabu",
                                "Kamis",
                                "Jumat",
                                "Sabtu"
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                fullHari.forEachIndexed { i, hari ->
                                    val dipilih = hariTerpilih.contains(hari)
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (dipilih) Blue500 else BgSubtle)
                                            .border(
                                                if (!dipilih) 1.dp else 0.dp,
                                                Blue100,
                                                CircleShape
                                            )
                                            .clickable {
                                                hariTerpilih =
                                                    if (dipilih) hariTerpilih - hari else hariTerpilih + hari
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            listHari[i],
                                            fontSize = 10.sp,
                                            fontWeight = if (dipilih) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (dipilih) White else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tombol simpan
            val canSave = nama.isNotEmpty() && (target.toIntOrNull() ?: 0) > 0

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (canSave) BtnGrad
                        else Brush.linearGradient(listOf(BgSubtle, BgSubtle))
                    )
                    .clickable(enabled = canSave) {
                        showSimpanDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Simpan Perubahan ✨",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (canSave) White else TextHint
                )
            }

            // Batal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))))
                    .clickable { onKembali() },
                contentAlignment = Alignment.Center
            ) {
                Text("Batal", color = Blue600, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
        }
    }

    // ── POPUP KONFIRMASI SIMPAN ────────────────────────────────────
    if (showSimpanDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A1628).copy(alpha = 0.72f))
                .clickable { showSimpanDialog = false },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .scale(popupScaleE)
                    .shadow(
                        elevation = 32.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = Blue500.copy(0.2f),
                        spotColor = Blue700.copy(0.25f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(White)
                    .border(2.dp, Blue100, RoundedCornerShape(28.dp))
                    .clickable(enabled = false) {}
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .background(BtnGrad)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 22.dp)
                    ) {

                        // Beruang bobble
                        Box(
                            modifier = Modifier.size(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(98.dp)
                                    .clip(CircleShape)
                                    .border(
                                        1.5.dp,
                                        Blue300.copy(alpha = ringAlphaE * 0.35f),
                                        CircleShape
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .size(78.dp)
                                    .clip(CircleShape)
                                    .background(Blue50)
                            )
                            Text(
                                "🐻",
                                fontSize = 38.sp,
                                modifier = Modifier.offset(y = bearBobE.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            "Simpan perubahan? ✏️",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blue700,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Data celengan akan diperbarui sekarang~",
                            fontSize = 12.sp,
                            color = Blue400,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))

                        // Preview perubahan
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Blue50)
                                .border(1.5.dp, Blue100, RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Nama", fontSize = 12.sp, color = Blue400)
                                Text(
                                    nama,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Blue700
                                )
                            }
                            HorizontalDivider(color = Blue100)
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Target", fontSize = 12.sp, color = Blue400)
                                Text(
                                    "Rp ${"%,d".format(target.toIntOrNull() ?: 0)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Blue700
                                )
                            }
                            HorizontalDivider(color = Blue100)
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Nominal", fontSize = 12.sp, color = Blue400)
                                Text(
                                    "Rp ${"%,d".format(nominal.toIntOrNull() ?: 0)} / ${jenis.lowercase()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Blue700
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Batal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Blue50)
                                    .border(1.5.dp, Blue100, RoundedCornerShape(14.dp))
                                    .clickable { showSimpanDialog = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Batal",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Blue600
                                )
                            }

                            // Simpan
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(50.dp)
                                    .shadow(
                                        10.dp, RoundedCornerShape(14.dp),
                                        ambientColor = Blue500.copy(0.35f),
                                        spotColor = Blue700.copy(0.4f)
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(BtnGrad)
                                    .clickable {
                                        // Simpan semua perubahan ke objek celengan
                                        celengan.nama = nama
                                        celengan.target = target.toIntOrNull() ?: celengan.target
                                        celengan.nominal = nominal.toIntOrNull() ?: celengan.nominal
                                        celengan.jenis = jenis
                                        celengan.image = imageUri?.toString()
                                        celengan.notifAktif = notifAktif
                                        celengan.jamNotif = jam
                                        celengan.hariNotif = hariTerpilih.toList()

                                        if (notifAktif) {
                                            val userId =
                                                FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                            scheduleNotification(context, jam, userId)
                                        }

                                        showSimpanDialog = false
                                        onSimpan()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        "✓",
                                        color = White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "Simpan!",
                                        color = White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  PROFILE SCREEN
// ═══════════════════════════════════════════════════════════════════

@Composable
fun ProfileScreen(
    listCelengan: List<Celengan>,
    onKembali: () -> Unit,
    onLogout: () -> Unit,
    onNavChange: (String) -> Unit,
    onTambah: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val email = currentUser?.email ?: "user@wishpay.app"

    var displayName by remember {
        mutableStateOf(
            currentUser?.displayName?.ifEmpty { null }
                ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
        )
    }

    val initials = displayName.split(" ").take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")

    var showEditName     by remember { mutableStateOf(false) }
    var showPassword     by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDataDiri     by remember { mutableStateOf(false) }
    var editNameValue    by remember { mutableStateOf(displayName) }
    var newPassword      by remember { mutableStateOf("") }
    var confirmPassword  by remember { mutableStateOf("") }
    var showPwText       by remember { mutableStateOf(false) }
    var isLoading        by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val notifCount = listCelengan.count { it.notifAktif }

    // ── Warna tema pink ───────────────────────────────────────────
    val ProfilePinkBg     = Color(0xFFFCE4EC)
    val ProfilePinkHeader = Color(0xFFF8BBD0)
    val ProfilePurpleBg   = Color(0xFFEDE7F6)
    val ProfileGreenBg    = Color(0xFFE8F5E9)
    val ProfileYellowBg   = Color(0xFFFFF9C4)
    val ProfileSectionLbl = Color(0xFFAAAAAA)
    val ProfileCardBorder = Color(0xFFF0E0E8)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F4))
    ) {
        if (showDataDiri) {
            DataDiriScreen(onKembali = { showDataDiri = false })
            return@Box
        }

        Scaffold(
            bottomBar = {
                WishPayBottomNav(
                    currentNav = "profil",
                    onNavChange = onNavChange,
                    onTambah = onTambah,
                    onProfil = { }
                )
            },
            containerColor = Color(0xFFFFF0F4)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)           // ← penting! agar tidak tertutup nav
                    .verticalScroll(rememberScrollState())
            ) {
                // ══ HEADER ══
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .background(ProfilePinkHeader)
                        .padding(top = 52.dp, bottom = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials.ifEmpty { "U" },
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = PinkMain
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = displayName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A148C)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = email,
                            fontSize = 13.sp,
                            color = Color(0xFF9E9E9E)
                        )
                        Spacer(Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.White)
                                .border(1.dp, ProfilePinkHeader, RoundedCornerShape(50.dp))
                                .clickable { showEditName = true }
                                .padding(horizontal = 28.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Edit Profil",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4A148C)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                ProfileSectionLabel("AKUN")
                Spacer(Modifier.height(8.dp))
                ProfileMenuCard(borderColor = ProfileCardBorder) {
                    ProfileMenuRow(
                        iconBg = ProfilePinkBg, iconText = "👤",
                        title = "Data Diri", showDivider = true,
                        onClick = { showDataDiri = true }
                    )
                    ProfileMenuRow(
                        iconBg = ProfilePurpleBg, iconText = "🔒",
                        title = "Keamanan & PIN", showDivider = false,
                        onClick = { showPassword = true }
                    )
                }

                Spacer(Modifier.height(20.dp))
                ProfileSectionLabel("PENGATURAN")
                Spacer(Modifier.height(8.dp))
                ProfileMenuCard(borderColor = ProfileCardBorder) {
                    ProfileMenuRow(
                        iconBg = ProfileGreenBg, iconText = "🔔",
                        title = "Notifikasi", trailingText = "Aktif",
                        showDivider = true,
                        onClick = { Toast.makeText(context, "Atur notifikasi di masing-masing celengan", Toast.LENGTH_SHORT).show() }
                    )
                    ProfileMenuRow(
                        iconBg = ProfileYellowBg, iconText = "🌙",
                        title = "Mode Gelap", trailingText = "Nonaktif",
                        showDivider = true,
                        onClick = { Toast.makeText(context, "Fitur mode gelap belum tersedia", Toast.LENGTH_SHORT).show() }
                    )
                    ProfileMenuRow(
                        iconBg = ProfilePinkBg, iconText = "🌐",
                        title = "Bahasa", trailingText = "Indonesia",
                        showDivider = false,
                        onClick = { Toast.makeText(context, "Fitur ganti bahasa belum tersedia", Toast.LENGTH_SHORT).show() }
                    )
                }

                Spacer(Modifier.height(20.dp))
                ProfileSectionLabel("LAINNYA")
                Spacer(Modifier.height(8.dp))
                ProfileMenuCard(borderColor = ProfileCardBorder) {
                    ProfileMenuRow(
                        iconBg = ProfilePurpleBg, iconText = "❓",
                        title = "Bantuan", showDivider = true,
                        onClick = { Toast.makeText(context, "Fitur bantuan belum tersedia", Toast.LENGTH_SHORT).show() }
                    )
                    ProfileMenuRow(
                        iconBg = ProfileGreenBg, iconText = "⭐",
                        title = "Beri Rating Aplikasi", showDivider = false,
                        onClick = { Toast.makeText(context, "Terima kasih atas dukunganmu!", Toast.LENGTH_SHORT).show() }
                    )
                }

                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ProfilePinkBg)
                        .border(1.dp, Color(0xFFF48FB1), RoundedCornerShape(14.dp))
                        .clickable { showLogoutDialog = true }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Keluar dari Akun",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PinkMain
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        // Popup-popup tetap di sini (showEditName, showPassword, showLogoutDialog)
        if (showEditName) { /* ... kode popup edit nama tetap ... */ }
        if (showPassword) { /* ... kode popup password tetap ... */ }
        if (showLogoutDialog) { /* ... kode popup logout tetap ... */ }
    }
}

// ── Helper: Label section ─────────────────────────────────────────
@Composable
private fun ProfileSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFFAAAAAA),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

// ── Helper: Card wrapper ──────────────────────────────────────────
@Composable
private fun ProfileMenuCard(
    borderColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(content = content)
    }
}

// ── Helper: Satu baris menu ───────────────────────────────────────
@Composable
private fun ProfileMenuRow(
    iconBg: Color,
    iconText: String,
    title: String,
    trailingText: String? = null,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    // Pilih icon Material berdasarkan teks emoji/key
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector = when (iconText) {
        "👤" -> Icons.Default.Person
        "🔒" -> Icons.Default.Notifications   // pakai Lock jika ada, fallback Notifications
        "🔔" -> Icons.Default.Notifications
        "🌙" -> Icons.Default.Timer
        "🌐" -> Icons.Default.Person
        "❓" -> Icons.Default.Person
        "⭐" -> Icons.Default.CalendarToday
        else -> Icons.Default.Person
    }

    // Warna tint icon per kategori
    val iconTint: Color = when (iconText) {
        "👤" -> Color(0xFFE91E8C)        // pink
        "🔒" -> Color(0xFF9575CD)        // purple
        "🔔" -> Color(0xFF43A047)        // green
        "🌙" -> Color(0xFFF9A825)        // yellow/amber
        "🌐" -> Color(0xFFE91E8C)        // pink
        "❓" -> Color(0xFF9575CD)        // purple
        "⭐" -> Color(0xFF43A047)        // green
        else -> Color(0xFF9E9E9E)
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // GANTI dengan:
            Box(
                modifier = Modifier
                    .size(46.dp)           // ← dari 38.dp jadi 46.dp (lebih besar)
                    .clip(RoundedCornerShape(14.dp))  // ← radius lebih besar
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)   // ← dari 20.dp jadi 24.dp
                )
            }

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF37474F),
                modifier = Modifier.weight(1f)
            )

            if (trailingText != null) {
                Text(
                    trailingText,
                    fontSize = 13.sp,
                    color = Color(0xFFAAAAAA)
                )
            } else {
                Icon(
                    Icons.Default.ArrowBack,
                    null,
                    tint = Color(0xFFCCCCCC),
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(180f)
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                color = Color(0xFFF5F5F5),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// ── Komponen menu item profil ──────────────────────────────────────
@Composable
fun ProfileMenuItem(
    icon: String,
    iconBg: Color,
    title: String,
    subtitle: String? = null,
    showDivider: Boolean = true,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 16.sp)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
            Icon(
                Icons.Default.ArrowBack,
                null,
                tint = Color(0xFFCDD5E0),
                modifier = Modifier
                    .size(14.dp)
                    .rotate(180f)
            )
        }
        if (showDivider) {
            HorizontalDivider(
                color = Color(0xFFF0F4FF),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

fun saveImageToInternalStorage(context: Context, uri: Uri): Uri {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw Exception("Gagal buka gambar")

    val fileName = "IMG_${System.currentTimeMillis()}.jpg"
    val file = File(context.filesDir, fileName)

    val outputStream = FileOutputStream(file)

    inputStream.copyTo(outputStream)

    inputStream.close()
    outputStream.close()

    return Uri.fromFile(file)
}

// ═══════════════════════════════════════════════════════════════════
//  DATA DIRI SCREEN
// ═══════════════════════════════════════════════════════════════════

@Composable
fun DataDiriScreen(
    onKembali: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current

    var namaLengkap  by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var namaPanggilan by remember { mutableStateOf(
        currentUser?.displayName?.split(" ")?.firstOrNull() ?: ""
    ) }
    var email        by remember { mutableStateOf(currentUser?.email ?: "") }
    var nomorHp      by remember { mutableStateOf("") }
    var tanggalLahir by remember { mutableStateOf("") }
    var jenisKelamin by remember { mutableStateOf("Perempuan") }
    var isLoading    by remember { mutableStateOf(false) }

    val initials = namaLengkap.split(" ").take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")

    val ProfilePinkHeader = Color(0xFFF8BBD0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F4))
    ) {
        // ── HEADER ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(ProfilePinkHeader)
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 24.dp)
        ) {
            // Tombol kembali
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.7f))
                    .clickable { onKembali() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ArrowBack, null,
                    tint = Color(0xFF4A148C),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Judul tengah
            Text(
                "Data Diri",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A148C),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // ── KONTEN SCROLLABLE ────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── CARD UTAMA ───────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // ── AVATAR ───────────────────────────────────────
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(PinkMain),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                initials.ifEmpty { "U" },
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        // Camera badge
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(PinkMain)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt, null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Ubah Foto Profil",
                        fontSize = 13.sp,
                        color = PinkMain,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(20.dp))

                    // ── FORM FIELDS ──────────────────────────────────

                    // Nama Lengkap
                    DataDiriField(
                        label = "Nama Lengkap",
                        value = namaLengkap,
                        onValueChange = { namaLengkap = it }
                    )

                    Spacer(Modifier.height(14.dp))

                    // Nama Panggilan
                    DataDiriField(
                        label = "Nama Panggilan",
                        value = namaPanggilan,
                        onValueChange = { namaPanggilan = it }
                    )

                    Spacer(Modifier.height(14.dp))

                    // Email (read only)
                    DataDiriField(
                        label = "Email",
                        value = email,
                        onValueChange = { },
                        readOnly = true
                    )

                    Spacer(Modifier.height(14.dp))

                    // Nomor HP
                    DataDiriField(
                        label = "Nomor HP",
                        value = nomorHp,
                        onValueChange = { nomorHp = it },
                        placeholder = "+62 812 3456 7890"
                    )

                    Spacer(Modifier.height(14.dp))

                    // Tanggal Lahir
                    DataDiriField(
                        label = "Tanggal Lahir",
                        value = tanggalLahir,
                        onValueChange = { tanggalLahir = it },
                        placeholder = "12 Januari 2000"
                    )

                    Spacer(Modifier.height(14.dp))

                    // ── Jenis Kelamin ────────────────────────────────
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Jenis Kelamin",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("Perempuan", "Laki-laki").forEach { gender ->
                                val isSelected = jenisKelamin == gender
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) Color(0xFFFFF0F4)
                                            else Color.White
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) PinkMain
                                            else Color(0xFFE0E0E0),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { jenisKelamin = gender },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        gender,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected)
                                            FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) PinkMain
                                        else Color(0xFF9E9E9E)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── TOMBOL SIMPAN ────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PinkMain)
                            .clickable(enabled = !isLoading) {
                                if (namaLengkap.isNotEmpty()) {
                                    isLoading = true
                                    val updates = com.google.firebase.auth.UserProfileChangeRequest
                                        .Builder()
                                        .setDisplayName(namaLengkap)
                                        .build()
                                    currentUser?.updateProfile(updates)
                                        ?.addOnSuccessListener {
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                "Data berhasil disimpan",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onKembali()
                                        }
                                        ?.addOnFailureListener {
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                "Gagal menyimpan data",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Nama lengkap tidak boleh kosong",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                "Simpan Perubahan",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Helper field untuk Data Diri ──────────────────────────────────
@Composable
private fun DataDiriField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    readOnly: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontSize = 12.sp,
            color = Color(0xFF9E9E9E),
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder.ifEmpty { label },
                    color = Color(0xFFBDBDBD),
                    fontSize = 14.sp
                )
            },
            readOnly = readOnly,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PinkMain,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = PinkMain,
                focusedTextColor = Color(0xFF37474F),
                unfocusedTextColor = Color(0xFF37474F),
                disabledBorderColor = Color(0xFFE0E0E0),
                disabledContainerColor = Color(0xFFF5F5F5),
                disabledTextColor = Color(0xFF9E9E9E)
            )
        )
    }
}


@Composable
fun NotificationScreen(
    notifications: List<NotificationItem>,
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Semua") }
    val filters = listOf("Semua", "Pengingat", "Aktivitas", "Target")

    val filtered = when (selectedFilter) {
        "Pengingat"  -> notifications.filter { it.type == "pengingat" }
        "Aktivitas"  -> notifications.filter {
            it.type == "aktivitas_masuk" || it.type == "aktivitas_keluar"
        }
        "Target"     -> notifications.filter {
            it.type == "target" || it.type == "progress"
        }
        else         -> notifications
    }

    // Urutan tanggal: Hari ini → Kemarin → tanggal lainnya
    val grouped = filtered.groupBy { it.date }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F4))
    ) {

        // ── HEADER ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(Color(0xFFF8BBD0))
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kiri: tombol back + judul
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.75f))
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack, null,
                            tint = Color(0xFF4A148C),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        "Notifikasi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A148C)
                    )
                }

                // Kanan: tombol "Tandai Dibaca"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE91E8C), RoundedCornerShape(50.dp))
                        .clickable { /* tandai semua dibaca */ }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Tandai Dibaca",
                        fontSize = 12.sp,
                        color = Color(0xFFE91E8C),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ── FILTER CHIPS ─────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 14.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                val isActive = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isActive) Color(0xFFE91E8C) else Color.White)
                        .border(
                            1.dp,
                            if (isActive) Color(0xFFE91E8C) else Color(0xFFE0C8D4),
                            RoundedCornerShape(50.dp)
                        )
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 18.dp, vertical = 9.dp)
                ) {
                    Text(
                        filter,
                        fontSize = 13.sp,
                        color = if (isActive) Color.White else Color(0xFF9E9E9E),
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        // ── ISI LIST ─────────────────────────────────────────────
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔔", fontSize = 40.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Belum ada notifikasi",
                        fontSize = 15.sp,
                        color = PinkSubText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Aktifkan pengingat di celenganmu",
                        fontSize = 13.sp,
                        color = PinkSubText
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = 8.dp, bottom = 32.dp
                )
            ) {
                grouped.forEach { (dateKey, items) ->

                    // Label tanggal section
                    item {
                        Text(
                            dateKey.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFAAAAAA),
                            letterSpacing = 0.6.sp,
                            modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)
                        )
                    }

                    // Kartu notifikasi
                    items(items) { notif ->
                        NotifCard(notif = notif)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun NotifCard(notif: NotificationItem) {

    // Tentukan warna & ikon sesuai tipe
    val iconBg: Color
    val iconTint: Color
    val iconLabel: String

    when (notif.type) {
        "aktivitas_masuk" -> {
            iconBg    = Color(0xFFE8F5E9)
            iconTint  = Color(0xFF43A047)
            iconLabel = "↑"
        }
        "aktivitas_keluar" -> {
            iconBg    = Color(0xFFFFEBEE)
            iconTint  = Color(0xFFEF5350)
            iconLabel = "↓"
        }
        "target" -> {
            iconBg    = Color(0xFFE8F5E9)
            iconTint  = Color(0xFF43A047)
            iconLabel = "✓"
        }
        "progress" -> {
            iconBg    = Color(0xFFFFF9C4)
            iconTint  = Color(0xFFF9A825)
            iconLabel = "!"
        }
        else -> {                               // "pengingat" (default)
            iconBg    = Color(0xFFF3E5F5)
            iconTint  = Color(0xFF9C27B0)
            iconLabel = "🔔"
        }
    }

    // Warna teks pesan untuk tipe tertentu
    val msgColor = when (notif.type) {
        "aktivitas_masuk" -> Color(0xFF43A047)
        else              -> Color(0xFF78909C)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color(0xFFF5E0EA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {

            // ── ICON ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                if (notif.type == "pengingat") {
                    Text(iconLabel, fontSize = 20.sp)
                } else {
                    Text(
                        iconLabel,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = iconTint
                    )
                }
            }

            // ── KONTEN TEKS ────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    notif.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF37474F)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    notif.message,
                    fontSize = 12.sp,
                    color = msgColor,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    notif.time,
                    fontSize = 11.sp,
                    color = Color(0xFFBBBBBB)
                )
            }

            // ── UNREAD DOT ─────────────────────────────────────────
            if (!notif.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE91E8C))
                )
            }
        }
    }
}