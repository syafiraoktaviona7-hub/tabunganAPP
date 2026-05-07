package com.example.tabunganapp

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


// Backgrounds — bersih, tenang, tidak ada gradasi mencolok
val BgPage = Color(0xFFF2F8FD)   // Alice blue — halus & bersih
val BgSurface = Color(0xFFFFFFFF)   // card / surface utama
val BgInput = Color(0xFFF0F6FB)   // input & chip background
val BgSubtle = Color(0xFFE8F3FA)   // divider / subtle bg

// Warna utama biru
val Blue50 = Color(0xFFE3F2FD)   // tint
val Blue100 = Color(0xFFBBDEFB)   // chip / badge bg
val Blue200 = Color(0xFF90CAF9)   // border subtle
val Blue300 = Color(0xFF64B5F6)   // soft accent
val Blue400 = Color(0xFF42A5F5)   // medium accent
val Blue500 = Color(0xFF2196F3)   // primary action
val Blue600 = Color(0xFF1E88E5)   // pressed / border
val Blue700 = Color(0xFF1565C0)   // header dark
val Blue800 = Color(0xFF0D47A1)   // dark text on blue bg

// Semantic
val GreenSoft = Color(0xFF66BB6A)   // success
val GreenBg = Color(0xFFE8F5E9)
val OrangeSoft = Color(0xFFFF8F00)   // warning / gold coin
val OrangeBg = Color(0xFFFFF8E1)
val RedSoft = Color(0xFFEF5350)

// Text
val TextPrimary = Color(0xFF1A2E42)   // heading utama
val TextSecondary = Color(0xFF546E7A)   // deskripsi
val TextHint = Color(0xFF90A4AE)   // placeholder
val White = Color(0xFFFFFFFF)

// Brush — hanya untuk header & tombol utama (subtle)
val HeaderGrad = Brush.linearGradient(listOf(Blue700, Blue500))
val BtnGrad = Brush.linearGradient(listOf(Blue600, Blue400))
val GreenGrad = Brush.linearGradient(listOf(Color(0xFF43A047), GreenSoft))


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
    val time: String
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

                        listCelengan = mutableListOf()

                        screen = "login"
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
            .background(Color(0xFFF0F4FF))
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
                    .background(Blue600),
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
            .background(Color(0xFFF0F4FF))
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
    focusedBorderColor = Color(0xFF4A90E2),
    unfocusedBorderColor = Color.LightGray,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = Color(0xFF4A90E2)
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
            .background(Color(0xFFD6EAFA)), // light sky blue persis seperti gambar
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
        containerColor = BgPage
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
//  BOTTOM NAV BAR
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
            .background(White)
            .border(BorderStroke(0.5.dp, Blue100), shape = RoundedCornerShape(0.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                label = "Beranda",
                isActive = currentNav == "home",
                svgPath = "home",
                onClick = { onNavChange("home") }
            )

            NavItem(
                label = "Statistik",
                isActive = currentNav == "statistik",
                svgPath = "statistik",
                onClick = { onNavChange("statistik") }
            )

            // Tombol + tengah
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(
                            6.dp, CircleShape,
                            ambientColor = Blue500.copy(0.3f),
                            spotColor = Blue700.copy(0.3f)
                        )
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Blue700, Blue500)))
                        .clickable { onTambah() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text("Tambah", fontSize = 10.sp, color = TextHint)
            }

            NavItem(
                label = "Aktivitas",
                isActive = currentNav == "aktivitas",
                svgPath = "aktivitas",
                onClick = { onNavChange("aktivitas") }
            )

            NavItem(
                label = "Profil",
                isActive = false,
                svgPath = "profil",
                onClick = { onProfil() }
            )
        }
    }
}

@Composable
fun RowScope.NavItem(
    label: String,
    isActive: Boolean,
    svgPath: String,
    onClick: () -> Unit,
    customIcon: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null
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
                .background(if (isActive) Blue50 else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            when (svgPath) {
                "home" -> Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = if (isActive) Blue700 else TextHint,
                    modifier = Modifier.size(20.dp)
                )

                "statistik" -> Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = if (isActive) Blue700 else TextHint,
                    modifier = Modifier.size(20.dp)
                )

                "aktivitas" -> Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = if (isActive) Blue700 else TextHint,
                    modifier = Modifier.size(20.dp)
                )

                "profil" -> Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isActive) Blue700 else TextHint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) Blue700 else TextHint
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
//  HOME SCREEN CONTENT — isi beranda (tanpa FAB & Scaffold)
// ═══════════════════════════════════════════════════════════════════


@Composable
fun HomeScreenContent(
    list: List<Celengan>,
    onClickItem: (Celengan) -> Unit,
    onBellClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val totalTarget = list.sumOf { it.target }
    val totalTerkumpul = list.sumOf { it.terkumpul }
    val overallProgress =
        if (totalTarget > 0) (totalTerkumpul.toFloat() / totalTarget).coerceIn(0f, 1f) else 0f
    val totalTercapai = list.count { it.terkumpul >= it.target }
    val auth = FirebaseAuth.getInstance()
    val email = auth.currentUser?.email ?: ""
    val displayName = auth.currentUser?.displayName?.ifEmpty { null }
        ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF0F4FF))) {

        // ── HEADER ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 20.dp)
        ) {
            Column {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Blue50),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_wishpay),
                                contentDescription = "wishPay",
                                modifier = Modifier.size(26.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Text(
                            "wishPay",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blue600
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Blue50)
                            .border(1.dp, Blue100, CircleShape)
                            .clickable { onBellClick() },  // ← TAMBAHKAN INI
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Notifications, null, tint = Blue600, modifier = Modifier.size(18.dp))

                        // Badge merah jika ada notif aktif
                        val notifCount = list.count { it.notifAktif }
                        if (notifCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(RedSoft)
                                    .border(1.5.dp, White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (notifCount > 9) "9+" else notifCount.toString(),
                                    fontSize = 7.sp,
                                    color = White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Greeting
                Text(
                    "Halo, $displayName! 👋",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    "Semangat Nabung!",
                    fontSize = 14.sp,
                    color = TextSecondary
                )

                Spacer(Modifier.height(16.dp))

                // Active badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Blue50)
                        .border(1.dp, Blue100, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🐷", fontSize = 14.sp)
                        Text(
                            "Tabungan Aktif: ${list.size}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Blue700
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Stat Cards ───────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Terkumpul
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = "💰",
                        iconBg = Color(0xFFE8F5E9),
                        label = "TERKUMPUL",
                        value = if (totalTerkumpul >= 1_000_000)
                            "Rp${String.format("%.1f", totalTerkumpul / 1_000_000f)}jt"
                        else "Rp${"%,d".format(totalTerkumpul)}",
                        isUnderlined = false
                    )
                    // Target Total
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = "🎯",
                        iconBg = Color(0xFFE3F2FD),
                        label = "TARGET TOTAL",
                        value = if (totalTarget >= 1_000_000)
                            "Rp${String.format("%.1f", totalTarget / 1_000_000f)}jt"
                        else "Rp${"%,d".format(totalTarget)}",
                        isUnderlined = true
                    )
                    // Tercapai
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = "✅",
                        iconBg = Color(0xFFFFF8E1),
                        label = "TERCAPAI",
                        value = "$totalTercapai Goal",
                        isUnderlined = false
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Progress Keseluruhan ──────────────────────
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFDDE3EE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Progres Keseluruhan",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                "${(overallProgress * 100).toInt()}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue600
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        // Progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFEEF2F8))
                        ) {
                            val animProg by animateFloatAsState(
                                overallProgress, tween(700, easing = EaseOutCubic), label = "prog"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animProg)
                                    .clip(RoundedCornerShape(50))
                                    .background(Blue600)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        val sisa = totalTarget - totalTerkumpul
                        if (sisa > 0) {
                            Text(
                                "Rp ${"%,d".format(sisa)} lagi untuk mencapai target utama kamu!",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── TAB BAR ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(White)
                .border(0.5.dp, Color(0xFFDDE3EE), RoundedCornerShape(12.dp))
                .padding(3.dp)
        ) {
            listOf("Berlangsung", "Tercapai").forEachIndexed { idx, label ->
                val selected = selectedTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) Blue600 else Color.Transparent)
                        .clickable { selectedTab = idx }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) White else TextSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── LIST ──────────────────────────────────────────────
        val filteredList = if (selectedTab == 0)
            list.filter { it.terkumpul < it.target }
        else
            list.filter { it.terkumpul >= it.target }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF0F4FF)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Blue50),
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
                    .background(Color(0xFFF0F4FF)),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { item ->
                    CelenganCard(item = item, onClick = { onClickItem(item) })
                }
                item { Spacer(Modifier.height(16.dp)) }
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
    isUnderlined: Boolean
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
                color = if (isUnderlined) Blue700 else TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  STATISTIK SCREEN
// ═══════════════════════════════════════════════════════════════════

@Composable
fun StatistikScreen(list: List<Celengan>) {
    val totalTerkumpul = list.sumOf { it.terkumpul }
    val totalTarget = list.sumOf { it.target }
    val totalTrx = list.sumOf { it.riwayat.size }
    val totalAktif = list.count { it.terkumpul < it.target }
    val totalTercapai = list.count { it.terkumpul >= it.target }
    val overallProg =
        if (totalTarget > 0) (totalTerkumpul.toFloat() / totalTarget).coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4FF))
    ) {
        // ── TOP BAR ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Blue50),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_wishpay),
                            contentDescription = "wishPay",
                            modifier = Modifier.size(26.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Text("wishPay", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Blue600)
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Blue50)
                        .border(1.dp, Blue100, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        null,
                        tint = Blue600,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Header judul + filter bulan ───────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Statistik",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(White)
                        .border(0.5.dp, Color(0xFFDDE3EE), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    val bulan = java.text.SimpleDateFormat("MMM yyyy", Locale("id"))
                        .format(java.util.Date())
                    Text(
                        bulan,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Card biru besar: Total Terkumpul ──────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Blue600)
                    .padding(20.dp)
            ) {
                // Dekorasi lingkaran blur di kanan
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.CenterEnd)
                        .offset(x = 20.dp)
                        .clip(CircleShape)
                        .alpha(0.15f)
                        .background(White)
                )
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-10).dp, y = 10.dp)
                        .clip(CircleShape)
                        .alpha(0.10f)
                        .background(White)
                )

                Column {
                    Text(
                        "Total Terkumpul",
                        fontSize = 13.sp,
                        color = White.copy(0.82f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Rp ${"%,d".format(totalTerkumpul)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(White.copy(0.18f))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text("↑", fontSize = 11.sp, color = White, fontWeight = FontWeight.Bold)
                            Text(
                                "+${(overallProg * 100).toInt()}% Bulan ini",
                                fontSize = 11.sp,
                                color = White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ── Card: Total Target ────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(0.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFDDE3EE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Total Target", fontSize = 13.sp, color = TextSecondary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Rp ${"%,d".format(totalTarget)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFEEF2F8))
                    ) {
                        val animProg by animateFloatAsState(
                            overallProg, tween(800, easing = EaseOutCubic), label = "statprog"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animProg)
                                .clip(RoundedCornerShape(50))
                                .background(Blue600)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${(overallProg * 100).toInt()}% dari seluruh impian tercapai",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // ── Row 3 stat kecil ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    Triple("↔", "TRANSAKSI", "$totalTrx"),
                    Triple("📋", "BERJALAN", "$totalAktif"),
                    Triple("✅", "TERCAPAI", "$totalTercapai")
                ).forEach { (icon, label, value) ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            Color(0xFFDDE3EE)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(icon, fontSize = 18.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                label,
                                fontSize = 9.sp,
                                color = TextSecondary,
                                letterSpacing = 0.3.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                value,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // ── Progres Impian ────────────────────────────────
            if (list.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Progres Impian",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "Lihat Semua",
                        fontSize = 13.sp,
                        color = Blue600,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFDDE3EE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        val iconMap = mapOf(
                            "Harian" to "🐷",
                            "Mingguan" to "📅",
                            "Bulanan" to "💰"
                        )
                        val colorList = listOf(Blue600, GreenSoft, OrangeSoft, RedSoft, Blue300)

                        list.forEachIndexed { i, cel ->
                            val p = if (cel.target > 0)
                                (cel.terkumpul.toFloat() / cel.target).coerceIn(0f, 1f) else 0f
                            val col = colorList[i % colorList.size]
                            val iconBgCol = when (i % colorList.size) {
                                0 -> Blue50
                                1 -> Color(0xFFE8F5E9)
                                2 -> Color(0xFFFFF8E1)
                                3 -> Color(0xFFFFEBEE)
                                else -> Blue50
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Ikon celengan
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(iconBgCol),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(iconMap[cel.jenis] ?: "🐷", fontSize = 18.sp)
                                }

                                Column(Modifier.weight(1f)) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            cel.nama,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "${(p * 100).toInt()}%",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = col
                                        )
                                    }
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        "Target: Rp ${"%,d".format(cel.target)}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFFEEF2F8))
                                    ) {
                                        val animP by animateFloatAsState(
                                            p, tween(700, i * 120), label = "ip$i"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(animP)
                                                .clip(RoundedCornerShape(50))
                                                .background(col)
                                        )
                                    }
                                }
                            }

                            if (i < list.lastIndex) {
                                Spacer(Modifier.height(14.dp))
                                HorizontalDivider(color = Color(0xFFEEF2F8))
                                Spacer(Modifier.height(14.dp))
                            }
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFDDE3EE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Belum ada celengan",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
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

    val totalMasuk = semuaAktivitas.filter { it.second.tipe == "MASUK" }.sumOf { it.second.nominal }
    val totalKeluar =
        semuaAktivitas.filter { it.second.tipe == "KELUAR" }.sumOf { it.second.nominal }
    val totalTrx = semuaAktivitas.size

    val today = SimpleDateFormat("dd MMM yyyy", Locale("id")).format(Date())
    val yesterday = SimpleDateFormat("dd MMM yyyy", Locale("id"))
        .format(Date(System.currentTimeMillis() - 86_400_000L))

    val filtered = when (filterAktif) {
        "Masuk" -> semuaAktivitas.filter { it.second.tipe == "MASUK" }
        "Keluar" -> semuaAktivitas.filter { it.second.tipe == "KELUAR" }
        "Hari ini" -> semuaAktivitas.filter {
            it.second.tanggal.contains(today)
        }

        else -> semuaAktivitas
    }

    // Kelompokkan berdasarkan tanggal (ambil bagian sebelum " •")
    val grouped = filtered.groupBy { (_, trx, _) ->
        trx.tanggal.substringBefore(" •").trim()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF0F4FF))) {

        // ── HEADER ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = HeaderGrad)
                .padding(horizontal = 20.dp)
                .padding(top = 48.dp, bottom = 20.dp)
        ) {
            Column {
                // Logo row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(White.copy(0.18f)),
                            contentAlignment = Alignment.Center
                        ) { Text("💰", fontSize = 16.sp) }
                        Text(
                            "wishPay",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(White.copy(0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications, null,
                            tint = White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    "Aktivitas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    "Semua riwayat tabunganmu",
                    fontSize = 12.sp,
                    color = White.copy(0.72f),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(Modifier.height(14.dp))

                // Summary chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AktSummaryChip(
                        label = "Total Masuk",
                        value = "+Rp ${"%,d".format(totalMasuk)}",
                        isGreen = true,
                        modifier = Modifier.weight(1f)
                    )
                    AktSummaryChip(
                        label = "Total Keluar",
                        value = "-Rp ${"%,d".format(totalKeluar)}",
                        isGreen = false,
                        modifier = Modifier.weight(1f)
                    )
                    AktSummaryChip(
                        label = "Transaksi",
                        value = "${totalTrx}x",
                        isGreen = null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── FILTER PILLS ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .padding(top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Semua", "Masuk", "Keluar", "Hari ini").forEach { f ->
                AktFilterPill(
                    text = f,
                    isActive = filterAktif == f,
                    onClick = { filterAktif = f }
                )
            }
        }

        // ── ISI ───────────────────────────────────────────────────
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF0F4FF)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F3FA)),
                        contentAlignment = Alignment.Center
                    ) { Text("🔔", fontSize = 32.sp) }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Belum ada aktivitas",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Mulai menabung untuk melihat aktivitas",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF0F4FF)),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                grouped.forEach { (dateKey, items) ->
                    // Label tanggal
                    item {
                        val dateLabel = when (dateKey) {
                            today -> "Hari ini — $dateKey"
                            yesterday -> "Kemarin — $dateKey"
                            else -> dateKey
                        }
                        Text(
                            dateLabel.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextHint,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(
                                start = 14.dp, end = 14.dp,
                                top = 10.dp, bottom = 4.dp
                            )
                        )
                    }
                    // Kartu per item
                    item {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            items.forEach { (namaCelengan, trx, _) ->
                                AktivitasCard(
                                    namaCelengan = namaCelengan,
                                    trx = trx
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
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
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFDDE3EE)),
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
    var input by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var nominalKeluar by remember { mutableStateOf(0) }
    var showSuksesDialog by remember { mutableStateOf(false) }
    var nominalSukses by remember { mutableStateOf(0) }
    var showTercapaiDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val progress =
        if (celengan.target > 0) (celengan.terkumpul.toFloat() / celengan.target).coerceIn(
            0f,
            1f
        ) else 0f
    val tercapai = celengan.terkumpul >= celengan.target

    Box(Modifier
        .fillMaxSize()
        .background(BgPage)) {
        Column(modifier = Modifier.fillMaxSize()) {

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
                    // Nav row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                        Text(
                            celengan.nama,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            textAlign = TextAlign.Center
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Tombol Edit
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(White.copy(0.18f))
                                    .clickable { onEdit() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    null,
                                    tint = White.copy(0.9f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            // Tombol Delete
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(White.copy(0.18f))
                                    .clickable { showDeleteDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    null,
                                    tint = White.copy(0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Progress display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Circular progress besar
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgress(progress = progress, size = 80, strokeWidth = 8f)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${(progress * 100).toInt()}%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                                Text("tercapai", fontSize = 9.sp, color = White.copy(0.7f))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Terkumpul", fontSize = 11.sp, color = White.copy(0.72f))
                            Text(
                                "Rp ${"%,d".format(celengan.terkumpul)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Target", fontSize = 10.sp, color = White.copy(0.6f))
                                    Text(
                                        "Rp ${"%,d".format(celengan.target)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = White.copy(0.9f)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Sisa", fontSize = 10.sp, color = White.copy(0.6f))
                                    Text(
                                        "Rp ${
                                            "%,d".format(
                                                (celengan.target - celengan.terkumpul).coerceAtLeast(
                                                    0
                                                )
                                            )
                                        }",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = White.copy(0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Stat chips — dengan icon yang menarik
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Estimasi hari
                    CleanCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Blue50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Blue500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                if (celengan.nominal > 0)
                                    "${
                                        (celengan.target - celengan.terkumpul).coerceAtLeast(0) / celengan.nominal.coerceAtLeast(
                                            1
                                        )
                                    } hari"
                                else "—",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Estimasi",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Per harian/mingguan/bulanan
                    CleanCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(OrangeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = OrangeSoft,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Rp ${"%,d".format(celengan.nominal)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Per ${celengan.jenis.lowercase()}",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Transaksi
                    CleanCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GreenBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = null,
                                    tint = GreenSoft,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "${celengan.riwayat.size}x",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Transaksi",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Notifikasi
                CleanCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                                    "Pengingat Menabung",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(celengan.jamNotif, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = celengan.notifAktif,
                            onCheckedChange = {
                                celengan.notifAktif = it
                                if (it) {
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                    scheduleNotification(context, celengan.jamNotif, userId)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Blue500
                            )
                        )
                    }
                }

                // Input nabung
                CleanCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Isi Tabungan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            label = { Text("Nominal (Rp)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue500,
                                focusedLabelColor = Blue500,
                                cursorColor = Blue500
                            ),
                            prefix = { Text("Rp ", color = TextHint) }
                        )

                        Spacer(Modifier.height(10.dp))

                        // Quick chips
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "5.000" to 5000,
                                "10.000" to 10000,
                                "50.000" to 50000
                            ).forEach { (label, value) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Blue50)
                                        .border(1.dp, Blue100, RoundedCornerShape(8.dp))
                                        .clickable { input = value.toString() }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        "Rp $label",
                                        fontSize = 12.sp,
                                        color = Blue600,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Tombol nabung
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BtnGrad)
                                .clickable {
                                    val tambah = input.toIntOrNull() ?: 0
                                    if (tambah > 0) {
                                        val userId =
                                            FirebaseAuth.getInstance().currentUser?.uid ?: ""

                                        // Update local state dulu
                                        celengan.terkumpul += tambah
                                        val now = SimpleDateFormat(
                                            "dd MMM yyyy • HH:mm",
                                            Locale.getDefault()
                                        )
                                            .format(Date())
                                        celengan.riwayat.add(
                                            Transaksi(
                                                tanggal = now,
                                                nominal = tambah,
                                                tipe = "MASUK"
                                            )
                                        )
                                        onUpdate()
                                        input = ""
                                        nominalSukses = tambah
                                        showSuksesDialog = true
                                        if (celengan.terkumpul >= celengan.target) {
                                            showTercapaiDialog = true
                                        }

                                        // Simpan ke Firestore di background
                                        if (userId.isNotEmpty() && celengan.id.isNotEmpty()) {
                                            val trxMasuk = Transaksi(
                                                tanggal = now,
                                                nominal = tambah,
                                                tipe = "MASUK"
                                            )
                                            scope.launch {
                                                try {
                                                    // Update saldo di Firestore
                                                    FirestoreManager.tambahSaldo(
                                                        userId = userId,
                                                        celenganId = celengan.id,
                                                        jumlah = tambah
                                                    )
                                                    // Simpan riwayat MASUK ke Firestore
                                                    FirestoreManager.tambahRiwayat(
                                                        userId = userId,
                                                        celenganId = celengan.id,
                                                        trx = trxMasuk
                                                    )
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nabung Sekarang",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = White
                            )
                        }

                        Spacer(Modifier.height(10.dp))

// Tombol kurangi tabungan
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(RedSoft)
                                .clickable {
                                    val kurang = input.toIntOrNull() ?: 0
                                    if (kurang > 0 && celengan.terkumpul >= kurang) {
                                        nominalKeluar = kurang
                                        showDialog = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Pakai Tabungan",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = White
                            )
                        }
                    }
                }

                val riwayatMasuk = celengan.riwayat.filter { it.tipe == "MASUK" }
                val riwayatKeluar = celengan.riwayat.filter { it.tipe == "KELUAR" }

                if (celengan.riwayat.isNotEmpty()) {

                    CleanCard(modifier = Modifier.fillMaxWidth()) {

                        Column(Modifier.padding(16.dp)) {

                            Text(
                                "Riwayat Transaksi",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(12.dp))

                            // ===============================
                            // 💰 UANG MASUK
                            // ===============================
                            if (riwayatMasuk.isNotEmpty()) {

                                Text(
                                    "Uang Masuk",
                                    color = GreenSoft,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(Modifier.height(8.dp))

                                riwayatMasuk.reversed().forEachIndexed { index, trx ->

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {

                                        Column {
                                            Text("Menabung", fontWeight = FontWeight.Medium)
                                            Text(
                                                trx.tanggal,
                                                fontSize = 10.sp,
                                                color = TextSecondary
                                            )
                                        }

                                        Badge(
                                            text = "+Rp ${"%,d".format(trx.nominal)}",
                                            bgColor = GreenBg,
                                            textColor = GreenSoft
                                        )
                                    }

                                    // ✅ Divider (STEP 4 — DISINI POSISINYA)
                                    if (index < riwayatMasuk.size - 1) {
                                        HorizontalDivider(
                                            color = BgSubtle,
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // ===============================
                            // 💸 UANG KELUAR
                            // ===============================
                            if (riwayatKeluar.isNotEmpty()) {

                                Text(
                                    "Pengeluaran",
                                    color = RedSoft,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(Modifier.height(8.dp))

                                riwayatKeluar.reversed().forEachIndexed { index, trx ->

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {

                                        Column {
                                            Text("Pakai Tabungan", fontWeight = FontWeight.Medium)
                                            Text(
                                                trx.tanggal,
                                                fontSize = 10.sp,
                                                color = TextSecondary
                                            )
                                        }

                                        Badge(
                                            text = "-Rp ${"%,d".format(trx.nominal)}",
                                            bgColor = Color(0xFFFFEBEE),
                                            textColor = RedSoft
                                        )
                                    }

                                    // ✅ Divider
                                    if (index < riwayatKeluar.size - 1) {
                                        HorizontalDivider(
                                            color = BgSubtle,
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Tombol kembali
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgSubtle)
                        .clickable { onKembali() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Kembali",
                        color = Blue600,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════
//  POPUP DELETE TABUNGAN — Blue + White BG
// ═══════════════════════════════════════════════════════════════════
        if (showDeleteDialog) {
            val scaleAnim by animateFloatAsState(
                targetValue = if (showDeleteDialog) 1f else 0.85f,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
                label = "scaleDelete"
            )
            val alphaAnim by animateFloatAsState(
                targetValue = if (showDeleteDialog) 1f else 0f,
                animationSpec = tween(250),
                label = "alphaDelete"
            )

            // Animasi pulse biru
            val infiniteBlue = rememberInfiniteTransition(label = "bluePulse")
            val bluePulse by infiniteBlue.animateFloat(
                initialValue = 0.93f,
                targetValue = 1.07f,
                animationSpec = infiniteRepeatable(
                    tween(850, easing = EaseInOutSine),
                    RepeatMode.Reverse
                ),
                label = "bluePulseScale"
            )
            val blueRingAlpha by infiniteBlue.animateFloat(
                initialValue = 0.25f,
                targetValue = 0.85f,
                animationSpec = infiniteRepeatable(
                    tween(950, easing = EaseInOutSine),
                    RepeatMode.Reverse
                ),
                label = "blueRingAlpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(alphaAnim)
                    .background(Color(0xFF000000).copy(alpha = 0.45f))
                    .clickable { showDeleteDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .scale(scaleAnim)
                        .shadow(
                            elevation = 32.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = Blue500.copy(0.18f),
                            spotColor = Blue700.copy(0.22f)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White)
                        .border(
                            1.5.dp,
                            Brush.verticalGradient(
                                listOf(Blue100, Blue50)
                            ),
                            RoundedCornerShape(28.dp)
                        )
                        .padding(26.dp)
                        .clickable(enabled = false) {}
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        // ── ICON dengan pulse ring ────────────────────
                        Box(
                            modifier = Modifier.size(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Ring paling luar — pulse
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(Color.Transparent)
                                    .border(
                                        1.5.dp,
                                        Blue300.copy(alpha = blueRingAlpha * 0.3f),
                                        CircleShape
                                    )
                            )
                            // Ring tengah
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(Blue50)
                                    .border(2.dp, Blue100, CircleShape)
                            )
                            // Icon utama pulse
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .scale(bluePulse)
                                    .shadow(
                                        12.dp, CircleShape,
                                        ambientColor = Blue500.copy(0.4f),
                                        spotColor = Blue700.copy(0.4f)
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Blue300, Blue500, Blue700)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🗑️", fontSize = 24.sp)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Text(
                            "Hapus Tabungan?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A2E42),
                            letterSpacing = (-0.5).sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            "Semua data akan dihapus permanen\ndan tidak bisa dikembalikan.",
                            fontSize = 13.sp,
                            color = Color(0xFF78909C),
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )

                        Spacer(Modifier.height(18.dp))

                        // ── Savings name box ──────────────────────────
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Blue50)
                                .border(1.5.dp, Blue100, RoundedCornerShape(16.dp))
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "TABUNGAN",
                                    fontSize = 10.sp,
                                    color = Blue300,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    celengan.nama,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Blue600
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // ── Stats row ─────────────────────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Terkumpul
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFECEFF1), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Terkumpul", fontSize = 10.sp, color = Color(0xFF90A4AE))
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Rp ${"%,d".format(celengan.terkumpul)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Blue500
                                    )
                                }
                            }
                            // Target
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFECEFF1), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Target", fontSize = 10.sp, color = Color(0xFF90A4AE))
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Rp ${"%,d".format(celengan.target)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A2E42)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // ── Warning box ───────────────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Blue50)
                                .border(1.dp, Blue100, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚠️", fontSize = 14.sp)
                            Text(
                                "Tindakan ini tidak bisa dibatalkan!",
                                fontSize = 12.sp,
                                color = Blue600,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(22.dp))

                        // ── Buttons ───────────────────────────────────
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Kembali
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                    .clickable { showDeleteDialog = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Kembali",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF78909C)
                                )
                            }

                            // Hapus
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
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Blue300, Blue500, Blue700)
                                        )
                                    )
                                    .clickable { onDelete(celengan) },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("🗑️", fontSize = 14.sp)
                                    Text(
                                        "Ya, Hapus",
                                        color = Color.White,
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


// ═══════════════════════════════════════════════════
//  POPUP KONFIRMASI PENGELUARAN — Red + White BG
// ═══════════════════════════════════════════════════
        if (showDialog) {
            val scaleAnim by animateFloatAsState(
                targetValue = if (showDialog) 1f else 0.85f,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
                label = "scaleDialog"
            )
            val alphaAnim by animateFloatAsState(
                targetValue = if (showDialog) 1f else 0f,
                animationSpec = tween(250),
                label = "alphaDialog"
            )

            // Animasi pulse pada icon
            val infinitePulse = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infinitePulse.animateFloat(
                initialValue = 0.94f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(
                    tween(800, easing = EaseInOutSine),
                    RepeatMode.Reverse
                ),
                label = "pulseIcon"
            )
            val ringAlpha by infinitePulse.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    tween(900, easing = EaseInOutSine),
                    RepeatMode.Reverse
                ),
                label = "ringAlpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(alphaAnim)
                    .background(Color(0xFF000000).copy(alpha = 0.45f))
                    .clickable { showDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .scale(scaleAnim)
                        .shadow(
                            elevation = 32.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = Color(0xFFEF5350).copy(0.18f),
                            spotColor = Color(0xFFB71C1C).copy(0.22f)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White)
                        .border(
                            1.5.dp,
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFCDD2), Color(0xFFFFEBEE))
                            ),
                            RoundedCornerShape(28.dp)
                        )
                        .padding(26.dp)
                        .clickable(enabled = false) {}
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        // ── ICON dengan pulse ring ────────────────────────
                        Box(
                            modifier = Modifier.size(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Ring luar animasi pulse
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(Color.Transparent)
                                    .border(
                                        1.5.dp,
                                        Color(0xFFEF5350).copy(alpha = ringAlpha * 0.25f),
                                        CircleShape
                                    )
                            )
                            // Ring tengah
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFEBEE))
                                    .border(2.dp, Color(0xFFFFCDD2), CircleShape)
                            )
                            // Icon utama dengan pulse
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .scale(pulseScale)
                                    .shadow(
                                        12.dp, CircleShape,
                                        ambientColor = Color(0xFFEF5350).copy(0.4f),
                                        spotColor = Color(0xFFB71C1C).copy(0.4f)
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFFEF9A9A),
                                                Color(0xFFEF5350),
                                                Color(0xFFC62828)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💸", fontSize = 24.sp)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Text(
                            "Konfirmasi Pengeluaran",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A2E42),
                            letterSpacing = (-0.5).sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            "Yakin ingin menggunakan tabunganmu?",
                            fontSize = 13.sp,
                            color = Color(0xFF78909C),
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )

                        Spacer(Modifier.height(18.dp))

                        // ── Amount Box ────────────────────────────────────
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFFEBEE))
                                .border(1.5.dp, Color(0xFFFFCDD2), RoundedCornerShape(16.dp))
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "JUMLAH",
                                    fontSize = 10.sp,
                                    color = Color(0xFFEF9A9A),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Rp ${"%,d".format(nominalKeluar)}",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF5350),
                                    letterSpacing = (-0.5).sp
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // ── Info rows ─────────────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFECEFF1), RoundedCornerShape(14.dp))
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            val sisaSaldo = celengan.terkumpul - nominalKeluar
                            listOf(
                                Triple(
                                    "Saldo saat ini",
                                    "Rp ${"%,d".format(celengan.terkumpul)}",
                                    false
                                ),
                                Triple(
                                    "Saldo setelah",
                                    "Rp ${"%,d".format(sisaSaldo.coerceAtLeast(0))}",
                                    true
                                ),
                                Triple(
                                    "Tanggal",
                                    java.text.SimpleDateFormat("dd MMM yyyy")
                                        .format(java.util.Date()),
                                    false
                                )
                            ).forEachIndexed { i, (label, value, isRed) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(label, fontSize = 13.sp, color = Color(0xFF90A4AE))
                                    Text(
                                        value,
                                        fontSize = 13.sp,
                                        color = if (isRed) Color(0xFFEF5350) else Color(0xFF1A2E42),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (i < 2) {
                                    HorizontalDivider(color = Color(0xFFECEFF1))
                                }
                            }
                        }

                        Spacer(Modifier.height(22.dp))

                        // ── Buttons ───────────────────────────────────────
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Batal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                    .clickable { showDialog = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Batal",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF78909C)
                                )
                            }

                            // Lanjutkan
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(50.dp)
                                    .shadow(
                                        10.dp, RoundedCornerShape(14.dp),
                                        ambientColor = Color(0xFFEF5350).copy(0.35f),
                                        spotColor = Color(0xFFC62828).copy(0.4f)
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFFEF9A9A),
                                                Color(0xFFEF5350),
                                                Color(0xFFC62828)
                                            )
                                        )
                                    )
                                    .clickable {
                                        val userId =
                                            FirebaseAuth.getInstance().currentUser?.uid ?: ""

                                        // Update local state dulu
                                        celengan.terkumpul -= nominalKeluar
                                        val now = SimpleDateFormat(
                                            "dd MMM yyyy • HH:mm",
                                            Locale.getDefault()
                                        )
                                            .format(Date())
                                        val trxKeluar = Transaksi(
                                            tanggal = now,
                                            nominal = nominalKeluar,
                                            tipe = "KELUAR"
                                        )
                                        celengan.riwayat.add(trxKeluar)
                                        onUpdate()
                                        input = ""
                                        showDialog = false

                                        // Simpan ke Firestore di background
                                        if (userId.isNotEmpty() && celengan.id.isNotEmpty()) {
                                            scope.launch {
                                                try {
                                                    // Kurangi saldo di Firestore
                                                    FirestoreManager.kurangiSaldo(
                                                        userId = userId,
                                                        celenganId = celengan.id,
                                                        jumlah = nominalKeluar
                                                    )
                                                    // Simpan riwayat KELUAR ke Firestore
                                                    FirestoreManager.tambahRiwayat(
                                                        userId = userId,
                                                        celenganId = celengan.id,
                                                        trx = trxKeluar
                                                    )
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        "✓",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "Lanjutkan",
                                        color = Color.White,
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

        if (showSuksesDialog) {

            val infiniteS = rememberInfiniteTransition(label = "sRing")
            val ringRotS by infiniteS.animateFloat(
                0f, 360f,
                infiniteRepeatable(tween(7000, easing = LinearEasing)),
                label = "rrs"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A1628).copy(alpha = 0.82f))
                    .clickable { showSuksesDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .shadow(
                            60.dp, RoundedCornerShape(32.dp),
                            ambientColor = Blue500.copy(0.3f),
                            spotColor = Blue700.copy(0.4f)
                        )
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFFFFF), Color(0xFFF0F8FF))
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.verticalGradient(
                                listOf(Color.White, Blue100)
                            ),
                            RoundedCornerShape(32.dp)
                        )
                        .padding(28.dp)
                        .clickable(enabled = false) {}
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        // Icon biru + spinning ring
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                            Canvas(modifier = Modifier.size(110.dp)) {
                                rotate(ringRotS) {
                                    drawArc(
                                        color = Blue500.copy(0.28f),
                                        startAngle = 0f, sweepAngle = 280f,
                                        useCenter = false,
                                        topLeft = androidx.compose.ui.geometry.Offset(4f, 4f),
                                        size = androidx.compose.ui.geometry.Size(
                                            size.width - 8f,
                                            size.height - 8f
                                        ),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            3f,
                                            cap = StrokeCap.Round
                                        )
                                    )
                                }
                            }
                            Canvas(modifier = Modifier.size(90.dp)) {
                                rotate(-ringRotS * 0.6f) {
                                    drawArc(
                                        color = Blue400.copy(0.14f),
                                        startAngle = 60f, sweepAngle = 200f,
                                        useCenter = false,
                                        topLeft = androidx.compose.ui.geometry.Offset(3f, 3f),
                                        size = androidx.compose.ui.geometry.Size(
                                            size.width - 6f,
                                            size.height - 6f
                                        ),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            2f,
                                            cap = StrokeCap.Round
                                        )
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .shadow(
                                        18.dp, CircleShape,
                                        ambientColor = Blue500.copy(0.5f),
                                        spotColor = Blue700.copy(0.6f)
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Blue400, Blue500, Blue700)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎉", fontSize = 30.sp)
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        Text(
                            "Nabung Berhasil! 🥳",
                            fontSize = 20.sp, fontWeight = FontWeight.Bold,
                            color = TextPrimary, letterSpacing = (-0.4).sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Kamu berhasil menambah tabungan sebesar",
                            fontSize = 13.sp, color = TextSecondary,
                            textAlign = TextAlign.Center, lineHeight = 20.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        // Nominal box biru
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Blue50, Blue100.copy(0.4f))
                                    )
                                )
                                .border(1.dp, Blue200, RoundedCornerShape(18.dp))
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "DITAMBAHKAN",
                                    fontSize = 10.sp, color = TextHint,
                                    fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "+Rp ${"%,d".format(nominalSukses)}",
                                    fontSize = 28.sp, fontWeight = FontWeight.Bold,
                                    color = Blue600, letterSpacing = (-0.5).sp
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Info box biru
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Blue50)
                                .border(1.dp, Blue100, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Blue100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("★", fontSize = 14.sp, color = Blue600)
                                }
                                Text(
                                    "Terus semangat menabung ya! 💪",
                                    fontSize = 12.sp, color = Blue600,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(Modifier.height(22.dp))

                        // Tombol Sip Lanjut — biru
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .shadow(
                                    14.dp, RoundedCornerShape(16.dp),
                                    ambientColor = Blue500.copy(0.45f),
                                    spotColor = Blue700.copy(0.55f)
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(BtnGrad)
                                .clickable { showSuksesDialog = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "✓",
                                    color = White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Sip, Lanjut!",
                                    color = White, fontWeight = FontWeight.Bold, fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════
//  POPUP TABUNGAN TERCAPAI — Blue + White BG
// ═══════════════════════════════════════════════════
        if (showTercapaiDialog) {
            val scaleAnimT by animateFloatAsState(
                targetValue = if (showTercapaiDialog) 1f else 0.8f,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
                label = "scaleTercapai"
            )
            val infiniteT = rememberInfiniteTransition(label = "tercapai")
            val pulseT by infiniteT.animateFloat(
                initialValue = 0.94f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(
                    tween(900, easing = EaseInOutSine),
                    RepeatMode.Reverse
                ),
                label = "iconPulse"
            )
            val ringAlphaT by infiniteT.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    tween(1100, easing = EaseInOutSine),
                    RepeatMode.Reverse
                ),
                label = "ringAlphaT"
            )
            val dotAlpha1 by infiniteT.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(500, easing = EaseInOutSine),
                    RepeatMode.Reverse
                ),
                label = "dot1"
            )
            val dotAlpha2 by infiniteT.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(
                        500,
                        delayMillis = 160,
                        easing = EaseInOutSine
                    ), RepeatMode.Reverse
                ),
                label = "dot2"
            )
            val dotAlpha3 by infiniteT.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(
                        500,
                        delayMillis = 320,
                        easing = EaseInOutSine
                    ), RepeatMode.Reverse
                ),
                label = "dot3"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF000000).copy(alpha = 0.45f))
                    .clickable { showTercapaiDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 22.dp)
                        .scale(scaleAnimT)
                        .shadow(
                            elevation = 28.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = Blue500.copy(0.18f),
                            spotColor = Blue700.copy(0.22f)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(White)
                        .border(1.5.dp, Blue100, RoundedCornerShape(28.dp))
                        .clickable(enabled = false) {}
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        // Garis biru tipis di atas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(BtnGrad)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {

                            // ── ICON dengan double pulse ring ────────────
                            Box(
                                modifier = Modifier.size(92.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Ring paling luar
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape)
                                        .border(
                                            1.5.dp,
                                            Blue200.copy(ringAlphaT * 0.4f),
                                            CircleShape
                                        )
                                )
                                // Ring tengah
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(Blue50)
                                        .border(2.dp, Blue100, CircleShape)
                                )
                                // Icon utama pulse
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .scale(pulseT)
                                        .shadow(
                                            10.dp, CircleShape,
                                            ambientColor = Blue500.copy(0.35f),
                                            spotColor = Blue700.copy(0.35f)
                                        )
                                        .clip(CircleShape)
                                        .background(BtnGrad),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🏆", fontSize = 26.sp)
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            // Dots animasi
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(dotAlpha1, dotAlpha2, dotAlpha3).forEachIndexed { i, alpha ->
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .alpha(alpha)
                                            .clip(CircleShape)
                                            .background(if (i == 1) Blue300 else Blue500)
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            Text(
                                "Tabungan Tercapai! 🎉",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = (-0.4).sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                "Selamat! Kamu berhasil mencapai\ntarget tabungan. Impianmu sudah\ndi depan mata!",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )

                            Spacer(Modifier.height(18.dp))

                            // ── Nama tabungan box ─────────────────────────
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Blue50)
                                    .border(1.5.dp, Blue100, RoundedCornerShape(16.dp))
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "TABUNGAN",
                                        fontSize = 10.sp,
                                        color = Blue400,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.8.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        celengan.nama,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Blue700,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // ── Stats row ─────────────────────────────────
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf(
                                    Triple(
                                        "Terkumpul",
                                        "Rp ${"%,d".format(celengan.terkumpul)}",
                                        true
                                    ),
                                    Triple("Target", "Rp ${"%,d".format(celengan.target)}", false)
                                ).forEach { (label, value, isBlue) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(BgSubtle)
                                            .border(0.5.dp, Blue100, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(label, fontSize = 10.sp, color = TextSecondary)
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                value,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isBlue) Blue500 else TextPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // ── Progress bar 100% ─────────────────────────
                            Column {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Progress", fontSize = 12.sp, color = TextSecondary)
                                    Text(
                                        "100%", fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold, color = Blue500
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Blue50)
                                        .border(0.5.dp, Blue100, RoundedCornerShape(50))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(50))
                                            .background(BtnGrad)
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // ── Info box ──────────────────────────────────
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Blue50)
                                    .border(0.5.dp, Blue100, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Blue100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("★", fontSize = 14.sp, color = Blue600)
                                }
                                Text(
                                    "Luar biasa! Disiplin menabungmu terbayar!",
                                    fontSize = 12.sp,
                                    color = Blue700,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(Modifier.height(20.dp))

                            // ── Tombol ────────────────────────────────────
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .shadow(
                                        10.dp, RoundedCornerShape(14.dp),
                                        ambientColor = Blue500.copy(0.35f),
                                        spotColor = Blue700.copy(0.4f)
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(BtnGrad)
                                    .clickable { showTercapaiDialog = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🏆", fontSize = 16.sp)
                                    Text(
                                        "Yeay, Terima Kasih!",
                                        color = White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
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
//  TAMBAH SCREEN
// ═══════════════════════════════════════════════════════════════════

@Composable
fun TambahScreen(
    onSimpan: (Celengan) -> Unit,
    onKembali: () -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var nominal by remember { mutableStateOf("") }
    var inputNabung by remember { mutableStateOf("") }
    var jenis by remember { mutableStateOf("Harian") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var notifAktif by remember { mutableStateOf(false) }
    var jam by remember { mutableStateOf("09:00") }
    var hariTerpilih by remember { mutableStateOf(setOf<String>()) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { imageUri = saveImageToInternalStorage(context, it) }
    }

    // Hitung sisa target secara realtime
    val targetInt = target.toIntOrNull() ?: 0
    val nabungInt = inputNabung.toIntOrNull() ?: 0
    val sisaTarget = (targetInt - nabungInt).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4FF))
    ) {
        // ── TOP BAR ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F4FF))
                        .border(0.5.dp, Color(0xFFDDE3EE), CircleShape)
                        .clickable { onKembali() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ArrowBack, null,
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    "Tambah Tabungan",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // ── FORM ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Foto picker ───────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 140.dp, height = 140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(White)
                    .border(
                        1.dp,
                        if (imageUri != null) Blue300 else Color(0xFFDDE3EE),
                        RoundedCornerShape(16.dp)
                    )
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
                    // Overlay ganti foto
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(TextPrimary.copy(0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CameraAlt, null,
                                tint = White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("Ganti Foto", fontSize = 11.sp, color = White)
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
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFEEF2F8))
                                .border(
                                    1.dp,
                                    Color(0xFFDDE3EE),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt, null,
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            "Tambah Foto",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "(Opsional)",
                            fontSize = 11.sp,
                            color = TextHint
                        )
                    }
                }
            }

            // ── Nama Tabungan ─────────────────────────────────
            FormLabel("Nama Tabungan")
            OutlinedTextField(
                value = nama,
                onValueChange = { nama = it },
                placeholder = { Text("Misal: Liburan ke Jepang", color = TextHint) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = cleanFieldColors(),
                singleLine = true
            )

            // ── Target Tabungan ───────────────────────────────
            FormLabel("Target Tabungan (Rp)")
            OutlinedTextField(
                value = target,
                onValueChange = { target = it },
                placeholder = { Text("0", color = TextHint) },
                prefix = { Text("Rp  ", color = TextSecondary, fontWeight = FontWeight.Medium) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = cleanFieldColors(),
                singleLine = true
            )

            // ── Frekuensi Menabung ────────────────────────────
            FormLabel("Frekuensi Menabung")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(White)
                    .border(0.5.dp, Color(0xFFDDE3EE), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Harian", "Mingguan", "Bulanan").forEach { j ->
                    val dipilih = jenis == j
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (dipilih) Blue600 else Color.Transparent)
                            .clickable { jenis = j }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            j,
                            fontSize = 13.sp,
                            fontWeight = if (dipilih) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (dipilih) White else TextSecondary
                        )
                    }
                }
            }

            // ── Jumlah Nabung Hari Ini ────────────────────────
            FormLabel("Jumlah Nabung Hari Ini")
            OutlinedTextField(
                value = inputNabung,
                onValueChange = { inputNabung = it },
                placeholder = { Text("0", color = TextHint) },
                prefix = { Text("Rp  ", color = TextSecondary, fontWeight = FontWeight.Medium) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = cleanFieldColors(),
                singleLine = true
            )
            // Sisa target hint
            Text(
                "Sisa target: Rp ${"%,d".format(sisaTarget)}",
                fontSize = 12.sp,
                color = if (nabungInt > 0) Blue500 else TextHint,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )

            // ── Nominal per periode ───────────────────────────
            FormLabel("Nominal ${jenis}")
            OutlinedTextField(
                value = nominal,
                onValueChange = { nominal = it },
                placeholder = { Text("0", color = TextHint) },
                prefix = { Text("Rp  ", color = TextSecondary, fontWeight = FontWeight.Medium) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = cleanFieldColors(),
                singleLine = true
            )

            // ── Pengingat Menabung ────────────────────────────
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(0.dp),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp, Color(0xFFDDE3EE)
                ),
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
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Blue50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Notifications, null,
                                    tint = Blue600,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                "Pengingat Menabung",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Switch(
                            checked = notifAktif,
                            onCheckedChange = { notifAktif = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Blue600,
                                uncheckedThumbColor = Color(0xFFCDD5E0),
                                uncheckedTrackColor = Color(0xFFEEF2F8)
                            )
                        )
                    }

                    AnimatedVisibility(visible = notifAktif) {
                        Column {
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = Color(0xFFEEF2F8))
                            Spacer(Modifier.height(16.dp))

                            // Waktu pengingat
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Waktu Pengingat",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Blue50)
                                        .border(1.dp, Blue100, RoundedCornerShape(10.dp))
                                        .clickable {
                                            val parts = jam.split(":")
                                            TimePickerDialog(
                                                context, { _, h, m ->
                                                    jam = String.format("%02d:%02d", h, m)
                                                },
                                                parts[0].toInt(),
                                                parts[1].toInt(),
                                                true
                                            ).show()
                                        }
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
                                            color = Blue600
                                        )
                                        Icon(
                                            Icons.Default.Timer, null,
                                            tint = Blue400,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            // Ulangi pada hari
                            Text(
                                "Ulangi Pada",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(10.dp))

                            val listHari = listOf("S", "S", "R", "K", "J", "S", "M")
                            val fullHari = listOf(
                                "Minggu", "Senin", "Selasa",
                                "Rabu", "Kamis", "Jumat", "Sabtu"
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
                                                if (dipilih) Blue600
                                                else Color(0xFFEEF2F8)
                                            )
                                            .border(
                                                if (!dipilih) 0.5.dp else 0.dp,
                                                Color(0xFFDDE3EE),
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
                                            listHari[i],
                                            fontSize = 11.sp,
                                            fontWeight = if (dipilih)
                                                FontWeight.Bold else FontWeight.Normal,
                                            color = if (dipilih) White else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Tombol Simpan ─────────────────────────────────
            val canSave = nama.isNotEmpty() && (target.toIntOrNull() ?: 0) > 0

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(
                        if (canSave) 8.dp else 0.dp,
                        RoundedCornerShape(14.dp),
                        ambientColor = Blue600.copy(0.3f),
                        spotColor = Blue700.copy(0.3f)
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (canSave) Blue600 else Color(0xFFDDE3EE))
                    .clickable(enabled = canSave) {
                        val t = target.toIntOrNull() ?: 0
                        val n = nominal.toIntOrNull() ?: 0
                        val nabung = inputNabung.toIntOrNull() ?: 0
                        if (notifAktif && jam.isNotEmpty()) {
                            val userId =
                                FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            scheduleNotification(context, jam, userId)
                        }
                        onSimpan(
                            Celengan(
                                nama = nama,
                                target = t,
                                terkumpul = nabung,
                                image = imageUri?.toString(),
                                nominal = n,
                                jenis = jenis,
                                notifAktif = notifAktif,
                                jamNotif = jam,
                                hariNotif = hariTerpilih.toList()
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Simpan Tabungan",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canSave) White else TextHint
                )
            }

            // Hint bawah
            if (notifAktif) {
                Text(
                    "Kamu akan diingatkan untuk menabung agar\nimpianmu cepat tercapai! 🚀",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ── Helper: label form ─────────────────────────────────────────────


// ── Helper: warna field clean ──────────────────────────────────────

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
    onLogout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val email = currentUser?.email ?: "user@wishpay.app"

    var displayName by remember {
        mutableStateOf(
            currentUser?.displayName?.ifEmpty { null }
                ?: email.substringBefore("@")
                    .replaceFirstChar { it.uppercase() }
        )
    }

    val joinedDate = remember {
        val ts = currentUser?.metadata?.creationTimestamp ?: 0L
        if (ts > 0L) java.text.SimpleDateFormat("MMMM yyyy", Locale("id"))
            .format(java.util.Date(ts)) else "—"
    }

    var showEditName by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var editNameValue by remember { mutableStateOf(displayName) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPwText by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    val userId = currentUser?.uid ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            val saved = FirestoreManager.loadProfileImage(userId)
            if (!saved.isNullOrEmpty()) profileImageUri = Uri.parse(saved)
        }
    }

    val profileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val saved = saveImageToInternalStorage(context, it)
            profileImageUri = saved
            kotlinx.coroutines.MainScope().launch {
                FirestoreManager.saveProfileImage(userId, saved.toString())
            }
        }
    }

    // ── Stats ──────────────────────────────────────────────────────
    val totalTerkumpul = listCelengan.sumOf { it.terkumpul }
    val totalTarget = listCelengan.sumOf { it.target }
    val totalTercapai = listCelengan.count { it.terkumpul >= it.target }
    val totalTrx = listCelengan.sumOf { it.riwayat.size }
    val notifCount = listCelengan.count { it.notifAktif }

    // ── Badges ─────────────────────────────────────────────────────
    val badges = listOf(
        BadgeData("⭐", "Pemula", "Buat celengan pertama", listCelengan.isNotEmpty()),
        BadgeData("✦", "Rajin", "7 transaksi", totalTrx >= 7),
        BadgeData("✓", "Konsisten", "30 transaksi", totalTrx >= 30),
        BadgeData("🏅", "Pemenang", "Target tercapai", totalTercapai >= 1),
        BadgeData("🪙", "Kolektor", "5 celengan", listCelengan.size >= 5)
    )
    val badgeEarned = badges.count { it.earned }

    Box(Modifier
        .fillMaxSize()
        .background(Color(0xFFF0F4FF))) {
        Column(Modifier.fillMaxSize()) {

            // ── TOP BAR ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 20.dp)
                    .padding(top = 52.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Blue50),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_wishpay),
                                contentDescription = "wishPay",
                                modifier = Modifier.size(26.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Text(
                            "wishPay",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blue600
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Blue50)
                            .border(1.dp, Blue100, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications, null,
                            tint = Blue600,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // ── AVATAR + INFO ──────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White)
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier.size(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(CircleShape)
                                    .background(Blue50)
                                    .border(2.dp, Blue100, CircleShape)
                                    .clickable { profileLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (profileImageUri != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(profileImageUri),
                                        contentDescription = "foto profil",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text("🐻", fontSize = 38.sp)
                                }
                            }
                            // Edit button
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(Blue600)
                                    .border(2.dp, White, CircleShape)
                                    .clickable { profileLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Edit, null,
                                    tint = White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            displayName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(email, fontSize = 13.sp, color = TextSecondary)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Bergabung sejak $joinedDate",
                            fontSize = 12.sp,
                            color = TextHint
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // ── Sistem Pencapaian ──────────────────────────
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp, Color(0xFFDDE3EE)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Sistem Pencapaian",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    "$badgeEarned / ${badges.size} Badge",
                                    fontSize = 12.sp,
                                    color = Blue600,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                badges.forEach { badge ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (badge.earned) Blue50
                                                    else Color(0xFFF0F0F0)
                                                )
                                                .border(
                                                    1.5.dp,
                                                    if (badge.earned) Blue200
                                                    else Color(0xFFDDDDDD),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (badge.earned) {
                                                Text(badge.icon, fontSize = 20.sp)
                                            } else {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    null,
                                                    tint = Color(0xFFCCCCCC),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            badge.label,
                                            fontSize = 10.sp,
                                            color = if (badge.earned) TextPrimary
                                            else Color(0xFFBBBBBB),
                                            fontWeight = if (badge.earned)
                                                FontWeight.Medium else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Pengaturan Akun ────────────────────────────
                    Text(
                        "Pengaturan Akun",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp, Color(0xFFDDE3EE)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = "✏️",
                                iconBg = Blue50,
                                title = "Ubah Nama",
                                showDivider = true,
                                onClick = {
                                    editNameValue = displayName
                                    showEditName = true
                                }
                            )
                            ProfileMenuItem(
                                icon = "✉️",
                                iconBg = Blue50,
                                title = "Ubah Email",
                                showDivider = true,
                                onClick = {
                                    Toast.makeText(
                                        context,
                                        "Fitur ubah email belum tersedia",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                            ProfileMenuItem(
                                icon = "🔒",
                                iconBg = Blue50,
                                title = "Ubah Password",
                                showDivider = true,
                                onClick = { showPassword = true }
                            )
                            ProfileMenuItem(
                                icon = "🔔",
                                iconBg = Color(0xFFE8F5E9),
                                title = "Pengaturan Notifikasi",
                                subtitle = if (notifCount > 0)
                                    "$notifCount celengan aktif" else null,
                                showDivider = false,
                                onClick = {
                                    Toast.makeText(
                                        context,
                                        "Atur notifikasi di masing-masing celengan",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    }

                    // ── Tombol Keluar ──────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(White)
                            .border(0.5.dp, Color(0xFFFFCDD2), RoundedCornerShape(14.dp))
                            .clickable { showLogoutDialog = true }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                null,
                                tint = RedSoft,
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(180f)
                            )
                            Text(
                                "Keluar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RedSoft
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }

        // ── POPUP: Edit Nama ───────────────────────────────────────
        if (showEditName) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF000000).copy(0.5f))
                    .clickable { showEditName = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Ubah Nama",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(16.dp))
                        Spacer(Modifier.height(18.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF0F4FF))
                                    .border(0.5.dp, Color(0xFFDDE3EE), RoundedCornerShape(12.dp))
                                    .clickable { showEditName = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Batal",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Blue600)
                                    .clickable {
                                        if (editNameValue.isNotEmpty()) {
                                            isLoading = true
                                            val updates =
                                                com.google.firebase.auth.UserProfileChangeRequest
                                                    .Builder()
                                                    .setDisplayName(editNameValue)
                                                    .build()
                                            currentUser?.updateProfile(updates)
                                                ?.addOnSuccessListener {
                                                    isLoading = false
                                                    displayName = editNameValue
                                                    showEditName = false
                                                    Toast.makeText(
                                                        context,
                                                        "Nama berhasil diubah",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                ?.addOnFailureListener {
                                                    isLoading = false
                                                    Toast.makeText(
                                                        context,
                                                        "Gagal ubah nama",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "Simpan",
                                        fontSize = 13.sp,
                                        color = White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── POPUP: Ganti Password ──────────────────────────────────
        if (showPassword) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF000000).copy(0.5f))
                    .clickable {
                        showPassword = false
                        newPassword = ""
                        confirmPassword = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Ubah Password",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Password Baru") },
                            placeholder = { Text("Minimal 8 karakter", color = TextHint) },
                            visualTransformation = if (showPwText)
                                VisualTransformation.None
                            else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPwText = !showPwText }) {
                                    Icon(
                                        Icons.Default.Visibility, null,
                                        tint = if (showPwText) Blue500 else TextHint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },

                            )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Ulangi password Anda") },
                            visualTransformation = PasswordVisualTransformation(),
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Visibility, null,
                                    tint = TextHint,
                                    modifier = Modifier.size(18.dp)
                                )
                            },

                            )
                        Spacer(Modifier.height(18.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF0F4FF))
                                    .border(
                                        0.5.dp,
                                        Color(0xFFDDE3EE),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        showPassword = false
                                        newPassword = ""
                                        confirmPassword = ""
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Batal",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Blue600)
                                    .clickable {
                                        when {
                                            newPassword.length < 6 ->
                                                Toast.makeText(
                                                    context,
                                                    "Minimal 6 karakter",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            newPassword != confirmPassword ->
                                                Toast.makeText(
                                                    context,
                                                    "Password tidak sama",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            else -> {
                                                isLoading = true
                                                currentUser?.updatePassword(newPassword)
                                                    ?.addOnSuccessListener {
                                                        isLoading = false
                                                        newPassword = ""
                                                        confirmPassword = ""
                                                        showPassword = false
                                                        Toast.makeText(
                                                            context,
                                                            "Password berhasil diubah",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    ?.addOnFailureListener {
                                                        isLoading = false
                                                        Toast.makeText(
                                                            context,
                                                            "Gagal. Coba re-login dulu.",
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
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "Simpan",
                                        fontSize = 13.sp,
                                        color = White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── POPUP: Logout ──────────────────────────────────────────
        if (showLogoutDialog) {
            val popupScale by animateFloatAsState(
                if (showLogoutDialog) 1f else 0.85f,
                spring(dampingRatio = 0.55f, stiffness = 400f),
                label = "logoutScale"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF000000).copy(0.5f))
                    .clickable { showLogoutDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(28.dp)
                        .scale(popupScale)
                        .clip(RoundedCornerShape(24.dp))
                        .background(White)
                        .border(0.5.dp, Color(0xFFDDE3EE), RoundedCornerShape(24.dp))
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Icon
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFEBEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🚪", fontSize = 28.sp)
                        }

                        Spacer(Modifier.height(14.dp))

                        Text(
                            "Yakin ingin keluar?",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Data tabunganmu tetap aman\ntersimpan di akun.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(Modifier.height(22.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF0F4FF))
                                    .border(
                                        0.5.dp,
                                        Color(0xFFDDE3EE),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { showLogoutDialog = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Batal",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RedSoft)
                                    .clickable {
                                        showLogoutDialog = false
                                        onLogout()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Ya, Keluar",
                                    color = White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
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


@Composable
fun NotificationScreen(
    notifications: List<NotificationItem>,
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4FF))
    ) {

        // HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 18.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F4FF))
                        .border(
                            0.5.dp,
                            Color(0xFFDDE3EE),
                            CircleShape
                        )
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        null,
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    "Notifikasi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // LIST NOTIF
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            items(notifications) { notif ->

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(
                        0.7.dp,
                        Color(0xFFDDE3EE)
                    )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Blue100),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "🔔",
                                fontSize = 20.sp
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                notif.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                notif.message,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                notif.time,
                                fontSize = 11.sp,
                                color = TextHint
                            )
                        }
                    }
                }
            }
        }
    }
}