package com.example.tabunganapp

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
val BgPage        = Color(0xFFF2F8FD)   // Alice blue — halus & bersih
val BgSurface     = Color(0xFFFFFFFF)   // card / surface utama
val BgInput       = Color(0xFFF0F6FB)   // input & chip background
val BgSubtle      = Color(0xFFE8F3FA)   // divider / subtle bg

// Warna utama biru
val Blue50        = Color(0xFFE3F2FD)   // tint
val Blue100       = Color(0xFFBBDEFB)   // chip / badge bg
val Blue200       = Color(0xFF90CAF9)   // border subtle
val Blue300       = Color(0xFF64B5F6)   // soft accent
val Blue400       = Color(0xFF42A5F5)   // medium accent
val Blue500       = Color(0xFF2196F3)   // primary action
val Blue600       = Color(0xFF1E88E5)   // pressed / border
val Blue700       = Color(0xFF1565C0)   // header dark
val Blue800       = Color(0xFF0D47A1)   // dark text on blue bg

// Semantic
val GreenSoft     = Color(0xFF66BB6A)   // success
val GreenBg       = Color(0xFFE8F5E9)
val OrangeSoft    = Color(0xFFFF8F00)   // warning / gold coin
val OrangeBg      = Color(0xFFFFF8E1)
val RedSoft       = Color(0xFFEF5350)

// Text
val TextPrimary   = Color(0xFF1A2E42)   // heading utama
val TextSecondary = Color(0xFF546E7A)   // deskripsi
val TextHint      = Color(0xFF90A4AE)   // placeholder
val White         = Color(0xFFFFFFFF)

// Brush — hanya untuk header & tombol utama (subtle)
val HeaderGrad    = Brush.linearGradient(listOf(Blue700, Blue500))
val BtnGrad       = Brush.linearGradient(listOf(Blue600, Blue400))
val GreenGrad     = Brush.linearGradient(listOf(Color(0xFF43A047), GreenSoft))


data class Celengan(
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

fun scheduleNotification(context: Context, jam: String, userId: String){


    try {
        if (jam.isEmpty() || !jam.contains(":")) return
        val parts  = jam.split(":")
        val hour   = parts[0].toIntOrNull() ?: return
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
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
            else
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
        }
    } catch (e: Exception) { e.printStackTrace() }
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


    var selected     by remember { mutableStateOf<Celengan?>(null) }
    var listCelengan by remember { mutableStateOf(mutableListOf<Celengan>()) }

    fun updateList() {
        listCelengan = listCelengan.toMutableList()
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    LaunchedEffect(userId) {
        try {
            if (userId.isNotEmpty()) {
                listCelengan = FirestoreManager.loadCelengan(userId).toMutableList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

// SAVE ke Firestore
    LaunchedEffect(listCelengan, userId) {
        try {
            if (userId.isNotEmpty()) {
                FirestoreManager.saveCelengan(userId, listCelengan)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    AnimatedContent(
        targetState = screen,
        transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(200)) },
        label = "nav"
    ) { target ->
        when (target) {
            "splash" -> SplashScreen {
                if (auth.currentUser != null) {
                    screen = "home"
                } else {
                    screen = "login"
                }
            }
            "home" -> MainScreen(
                currentNav   = currentNav,
                onNavChange  = { currentNav = it },
                list         = listCelengan,
                onTambah     = { screen = "tambah" },
                onProfil     = { screen = "profil" },
                onClickItem  = { selected = it; screen = "detail" },
                onLogout     = {
                    val uid = auth.currentUser?.uid ?: ""
                    cancelNotification(context, uid)
                    auth.signOut()
                    screen = "login"
                }
            )
            "tambah" -> TambahScreen(
                onSimpan  = { listCelengan = (listCelengan + it).toMutableList(); screen = "home" },
                onKembali = { screen = "home" }
            )
            "detail" -> selected?.let {
                DetailScreen(
                    celengan  = it,
                    onUpdate  = { updateList() },
                    onKembali = { screen = "home" },
                    onDelete  = { cel ->
                        listCelengan = listCelengan.filter { it != cel }.toMutableList()
                        screen = "home"
                    },
                    onEdit = { screen = "edit" }
                )
            }
            "edit" -> selected?.let {
                EditScreen(
                    celengan  = it,
                    onSimpan  = { updateList(); screen = "detail" },
                    onKembali = { screen = "detail" }
                )
            }
            "login" -> LoginScreen(
                onLoginSuccess = { screen = "home" },
                onGoRegister   = { screen = "register" }
            )
            "register" -> RegisterScreen(
                onRegisterSuccess = { screen = "home" },
                onGoLogin         = { screen = "login" }
            )
            "profil" -> ProfileScreen(
                listCelengan = listCelengan,
                onKembali    = { screen = "home" },
                onLogout     = {
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
    var isLoading by remember { mutableStateOf(false) }

    val infinite = rememberInfiniteTransition(label = "loginBg")
    val bearBob by infinite.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "bearBob"
    )
    val starAlpha1 by infinite.animateFloat(0.4f, 1f, infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse), label = "s1")
    val starAlpha2 by infinite.animateFloat(0.4f, 1f, infiniteRepeatable(tween(700, delayMillis = 200, easing = EaseInOutSine), RepeatMode.Reverse), label = "s2")
    val starAlpha3 by infinite.animateFloat(0.4f, 1f, infiniteRepeatable(tween(1100, delayMillis = 400, easing = EaseInOutSine), RepeatMode.Reverse), label = "s3")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE8F4FF), Color(0xFFF0F8FF), Color(0xFFDCEEFF))
                )
            )
    ) {
        // ── Blob background ──────────────────────────────────────
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .alpha(0.18f)
                .clip(CircleShape)
                .background(Blue400)
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .alpha(0.15f)
                .clip(CircleShape)
                .background(Blue300)
        )

        // ── Bintang dekorasi ─────────────────────────────────────
        Text("★", fontSize = 18.sp, color = Color(0xFFFFBF24),
            modifier = Modifier.offset(x = 32.dp, y = 80.dp).alpha(starAlpha1))
        Text("✦", fontSize = 13.sp, color = Blue300,
            modifier = Modifier.offset(x = 300.dp, y = 100.dp).alpha(starAlpha2))
        Text("★", fontSize = 10.sp, color = Color(0xFFFFBF24),
            modifier = Modifier.offset(x = 280.dp, y = 60.dp).alpha(starAlpha3))
        Text("◆", fontSize = 12.sp, color = Blue200,
            modifier = Modifier.align(Alignment.BottomStart).offset(x = 24.dp, y = (-180).dp).alpha(starAlpha2))

        // ── Konten utama ─────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 60.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Maskot beruang
            Box(
                modifier = Modifier
                    .offset(y = bearBob.dp)
            ) {
                BearMascot(isHappy = false)
            }

            Spacer(Modifier.height(20.dp))

            // Card form
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.92f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Judul
                    Text(
                        "Selamat Datang Kembali!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Blue700,
                        textAlign = TextAlign.Center,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Senang bertemu kamu lagi~ 💙",
                        fontSize = 13.sp,
                        color = Blue400,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    // Field Email
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "EMAIL",
                        placeholder = "nama@email.com",
                        leadingEmoji = "✉",
                        isPassword = false
                    )

                    Spacer(Modifier.height(12.dp))

                    // Field Password
                    AuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "PASSWORD",
                        placeholder = "Masukkan password...",
                        leadingEmoji = "🔒",
                        isPassword = true,
                        showPassword = showPassword,
                        onTogglePassword = { showPassword = !showPassword }
                    )

                    // Lupa password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {}) {
                            Text("Lupa password? 🤔", fontSize = 12.sp, color = Blue400, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Tombol masuk
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(8.dp, RoundedCornerShape(14.dp),
                                ambientColor = Blue500.copy(0.3f), spotColor = Blue700.copy(0.3f))
                            .clip(RoundedCornerShape(14.dp))
                            .background(BtnGrad)
                            .clickable(enabled = !isLoading) {
                                if (email.isNotEmpty() && password.isNotEmpty()) {
                                    isLoading = true
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnSuccessListener { onLoginSuccess() }
                                        .addOnFailureListener {
                                            isLoading = false
                                            Toast.makeText(context, "Login gagal 😢", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(context, "Isi semua dulu 😅", Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                        } else {
                            Text("Masuk Sekarang ✨", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = White)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Divider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Blue100))
                        Text("  atau  ", fontSize = 11.sp, color = Blue200, fontWeight = FontWeight.SemiBold)
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Blue100))
                    }

                    Spacer(Modifier.height(12.dp))

                    // Link ke register
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Belum punya akun? ", fontSize = 13.sp, color = Blue300, fontWeight = FontWeight.Medium)
                        Text(
                            "Daftar di sini! 🚀",
                            fontSize = 13.sp,
                            color = Blue600,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.clickable { onGoRegister() }
                        )
                    }
                }
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

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val infinite = rememberInfiniteTransition(label = "regBg")
    val bearBob by infinite.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "bearBobReg"
    )
    val starAlpha1 by infinite.animateFloat(0.4f, 1f, infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse), label = "rs1")
    val starAlpha2 by infinite.animateFloat(0.4f, 1f, infiniteRepeatable(tween(700, delayMillis = 300, easing = EaseInOutSine), RepeatMode.Reverse), label = "rs2")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE8F4FF), Color(0xFFF0F8FF), Color(0xFFDCEEFF))
                )
            )
    ) {
        // Blob background
        Box(modifier = Modifier.size(260.dp).offset(x = (-70).dp, y = (-50).dp)
            .alpha(0.16f).clip(CircleShape).background(Blue400))
        Box(modifier = Modifier.size(180.dp).align(Alignment.BottomEnd)
            .offset(x = 50.dp, y = 50.dp).alpha(0.14f).clip(CircleShape).background(Blue300))

        // Bintang
        Text("★", fontSize = 18.sp, color = Color(0xFFFFBF24),
            modifier = Modifier.offset(x = 30.dp, y = 70.dp).alpha(starAlpha1))
        Text("✦", fontSize = 14.sp, color = Blue300,
            modifier = Modifier.offset(x = 295.dp, y = 90.dp).alpha(starAlpha2))
        Text("◆", fontSize = 11.sp, color = Blue200,
            modifier = Modifier.align(Alignment.BottomStart).offset(x = 20.dp, y = (-120).dp).alpha(starAlpha1))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 50.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Maskot beruang senang
            Box(modifier = Modifier.offset(y = bearBob.dp)) {
                BearMascot(isHappy = true)
            }

            Spacer(Modifier.height(16.dp))

            // Card form
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.92f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Buat Akun Baru!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Blue700,
                        textAlign = TextAlign.Center,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Yuk mulai perjalananmu bersama kami 💙",
                        fontSize = 12.sp,
                        color = Blue400,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(22.dp))

                    // Email
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "EMAIL",
                        placeholder = "nama@email.com",
                        leadingEmoji = "✉",
                        isPassword = false
                    )

                    Spacer(Modifier.height(12.dp))

                    // Password
                    AuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "PASSWORD",
                        placeholder = "Buat password kuat...",
                        leadingEmoji = "🔒",
                        isPassword = true,
                        showPassword = showPassword,
                        onTogglePassword = { showPassword = !showPassword }
                    )

                    Spacer(Modifier.height(12.dp))

                    // Konfirmasi password
                    AuthTextField(
                        value = confirm,
                        onValueChange = { confirm = it },
                        label = "KONFIRMASI PASSWORD",
                        placeholder = "Ulangi password...",
                        leadingEmoji = "🛡",
                        isPassword = true,
                        showPassword = showConfirm,
                        onTogglePassword = { showConfirm = !showConfirm }
                    )

                    Spacer(Modifier.height(20.dp))

                    // Tombol daftar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(8.dp, RoundedCornerShape(14.dp),
                                ambientColor = Blue500.copy(0.3f), spotColor = Blue700.copy(0.3f))
                            .clip(RoundedCornerShape(14.dp))
                            .background(BtnGrad)
                            .clickable(enabled = !isLoading) {
                                when {
                                    email.isEmpty() || password.isEmpty() || confirm.isEmpty() ->
                                        Toast.makeText(context, "Isi semua dulu 😅", Toast.LENGTH_SHORT).show()
                                    password != confirm ->
                                        Toast.makeText(context, "Password tidak sama 😢", Toast.LENGTH_SHORT).show()
                                    password.length < 6 ->
                                        Toast.makeText(context, "Password minimal 6 karakter 🔑", Toast.LENGTH_SHORT).show()
                                    else -> {
                                        isLoading = true
                                        auth.createUserWithEmailAndPassword(email, password)
                                            .addOnSuccessListener { onRegisterSuccess() }
                                            .addOnFailureListener {
                                                isLoading = false
                                                Toast.makeText(context, "Register gagal 😢\n${it.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                        } else {
                            Text("Daftar Sekarang 🎉", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = White)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Divider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Blue100))
                        Text("  atau  ", fontSize = 11.sp, color = Blue200, fontWeight = FontWeight.SemiBold)
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Blue100))
                    }

                    Spacer(Modifier.height(12.dp))

                    // Link ke login
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sudah punya akun? ", fontSize = 13.sp, color = Blue300, fontWeight = FontWeight.Medium)
                        Text(
                            "Masuk di sini! 😊",
                            fontSize = 13.sp,
                            color = Blue600,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.clickable { onGoLogin() }
                        )
                    }
                }
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
            .shadow(16.dp, CircleShape, ambientColor = Blue300.copy(0.2f), spotColor = Blue400.copy(0.2f)),
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
                    Text(leadingEmoji, fontSize = 16.sp,
                        modifier = Modifier.padding(start = 4.dp))
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
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = BgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

/** Progress bar biru bersih */
@Composable
fun CleanProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val anim by animateFloatAsState(
        targetValue   = progress.coerceIn(0f, 1f),
        animationSpec = tween(700, easing = EaseOutCubic),
        label         = "bar"
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
        targetValue   = progress.coerceIn(0f, 1f),
        animationSpec = tween(800, easing = EaseOutCubic),
        label         = "arc"
    )
    Canvas(modifier = modifier.size(size.dp)) {
        val sw      = strokeWidth
        val sweep   = anim * 360f
        val padding = sw / 2
        val topLeft = androidx.compose.ui.geometry.Offset(padding, padding)
        val arcSize = androidx.compose.ui.geometry.Size(
            this.size.width  - sw,
            this.size.height - sw
        )
        // Track
        drawArc(
            color      = BgSubtle,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter  = false,
            topLeft    = topLeft,
            size       = arcSize,
            style      = androidx.compose.ui.graphics.drawscope.Stroke(sw, cap = StrokeCap.Round)
        )
        // Fill
        if (sweep > 0f) {
            drawArc(
                color      = Blue400,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = androidx.compose.ui.graphics.drawscope.Stroke(sw, cap = StrokeCap.Round)
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
        infiniteRepeatable(tween(1800, delayMillis = 300, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "fy2"
    )
    val floatY3 by infinite.animateFloat(
        0f, -12f,
        infiniteRepeatable(tween(2500, delayMillis = 600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "fy3"
    )
    val floatY4 by infinite.animateFloat(
        0f, 20f,
        infiniteRepeatable(tween(1900, delayMillis = 900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "fy4"
    )
    val floatY5 by infinite.animateFloat(
        0f, -16f,
        infiniteRepeatable(tween(2100, delayMillis = 400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "fy5"
    )

    val logoScale by infinite.animateFloat(
        0.96f, 1.04f,
        infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "logo"
    )

    val dotScale1 by infinite.animateFloat(0.5f, 1.3f, infiniteRepeatable(tween(600, easing = EaseInOutSine), RepeatMode.Reverse), label = "d1")
    val dotScale2 by infinite.animateFloat(0.5f, 1.3f, infiniteRepeatable(tween(600, delayMillis = 200, easing = EaseInOutSine), RepeatMode.Reverse), label = "d2")
    val dotScale3 by infinite.animateFloat(0.5f, 1.3f, infiniteRepeatable(tween(600, delayMillis = 400, easing = EaseInOutSine), RepeatMode.Reverse), label = "d3")

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
                fontSize      = 36.sp,
                fontWeight    = FontWeight.Bold,
                color         = TextPrimary,
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
                    color    = TextPrimary,
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
    onLogout: () -> Unit
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (currentNav) {
                "home"      -> HomeScreenContent(list = list, onClickItem = onClickItem)
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
            // Beranda
            NavItem(
                icon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = if (currentNav == "home") Blue700 else TextHint,
                        modifier = Modifier.size(20.dp)
                    )
                },
                iconVector = Icons.Default.ArrowBack,
                label = "Beranda",
                isActive = currentNav == "home",
                customIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = if (currentNav == "home") Blue700 else TextHint,
                        modifier = Modifier.size(20.dp).rotate(0f)
                    )
                },
                svgPath = "home",
                onClick = { onNavChange("home") }
            )

            // Statistik
            NavItem(
                label = "Statistik",
                isActive = currentNav == "statistik",
                svgPath = "statistik",
                onClick = { onNavChange("statistik") }
            )

            // Tombol + di tengah
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(6.dp, CircleShape, ambientColor = Blue500.copy(0.3f), spotColor = Blue700.copy(0.3f))
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Blue700, Blue500)))
                        .clickable { onTambah() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text("Tambah", fontSize = 10.sp, color = TextHint)
            }

            // Aktivitas
            NavItem(
                label = "Aktivitas",
                isActive = currentNav == "aktivitas",
                svgPath = "aktivitas",
                onClick = { onNavChange("aktivitas") }
            )

            // Profil
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
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = if (isActive) Blue700 else TextHint,
                    modifier = Modifier.size(18.dp)
                )
                "statistik" -> Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = null,
                    tint = if (isActive) Blue700 else TextHint,
                    modifier = Modifier.size(18.dp)
                )
                "aktivitas" -> Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (isActive) Blue700 else TextHint,
                    modifier = Modifier.size(18.dp)
                )
                "profil" -> Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isActive) Blue700 else TextHint,
                    modifier = Modifier.size(18.dp)
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
    onClickItem: (Celengan) -> Unit
) {
    var selectedTab    by remember { mutableStateOf(0) }
    val totalTarget     = list.sumOf { it.target }
    val totalTerkumpul  = list.sumOf { it.terkumpul }
    val overallProgress = if (totalTarget > 0) (totalTerkumpul.toFloat() / totalTarget).coerceIn(0f, 1f) else 0f
    val totalTercapai   = list.count { it.terkumpul >= it.target }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── HEADER ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = HeaderGrad, shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .padding(horizontal = 20.dp)
                .padding(top = 48.dp, bottom = 24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
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
                            Text("wishPay", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = White)
                            Text("Halo, semangat nabung!", fontSize = 11.sp, color = White.copy(0.72f))
                        }
                    }
                    Badge(
                        text = "${list.size} Aktif",
                        bgColor = White.copy(alpha = 0.20f),
                        textColor = White
                    )
                }
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(White.copy(alpha = 0.15f)).padding(14.dp)) {
                        Column {
                            Text("Terkumpul", fontSize = 11.sp, color = White.copy(0.75f))
                            Spacer(Modifier.height(4.dp))
                            Text("Rp ${"%,d".format(totalTerkumpul)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                        }
                    }
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(White.copy(alpha = 0.15f)).padding(14.dp)) {
                        Column {
                            Text("Target Total", fontSize = 11.sp, color = White.copy(0.75f))
                            Spacer(Modifier.height(4.dp))
                            Text("Rp ${"%,d".format(totalTarget)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                        }
                    }
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(White.copy(alpha = 0.15f)).padding(14.dp)) {
                        Column {
                            Text("Tercapai", fontSize = 11.sp, color = White.copy(0.75f))
                            Spacer(Modifier.height(4.dp))
                            Text("$totalTercapai", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                            Text("celengan", fontSize = 10.sp, color = White.copy(0.7f))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Progress keseluruhan", fontSize = 11.sp, color = White.copy(0.75f))
                        Text("${(overallProgress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = White)
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(White.copy(0.22f))) {
                        val animProg by animateFloatAsState(overallProgress, tween(700, easing = EaseOutCubic), label = "hprog")
                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(animProg).clip(RoundedCornerShape(50)).background(White.copy(0.9f)))
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.padding(horizontal = 20.dp).clip(RoundedCornerShape(12.dp)).background(BgSubtle).padding(3.dp)) {
            listOf("Berlangsung", "Tercapai").forEachIndexed { idx, label ->
                val selected = selectedTab == idx
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(if (selected) White else Color.Transparent)
                        .clickable { selectedTab = idx }.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) Blue600 else TextSecondary)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        val filteredList = if (selectedTab == 0) list.filter { it.terkumpul < it.target } else list.filter { it.terkumpul >= it.target }
        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().background(BgPage), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(BgSubtle), contentAlignment = Alignment.Center) {
                        Image(painter = painterResource(id = R.drawable.logo_wishpay), contentDescription = null, modifier = Modifier.size(50.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(if (selectedTab == 0) "Belum ada tabungan" else "Belum ada yang tercapai", fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("Tap tombol + untuk mulai menabung", fontSize = 13.sp, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(BgPage),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { item -> CelenganCard(item = item, onClick = { onClickItem(item) }) }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  STATISTIK SCREEN
// ═══════════════════════════════════════════════════════════════════

@Composable
fun StatistikScreen(list: List<Celengan>) {
    val totalTerkumpul = list.sumOf { it.terkumpul }
    val totalTarget    = list.sumOf { it.target }
    val totalTrx       = list.sumOf { it.riwayat.size }
    val totalAktif     = list.count { it.terkumpul < it.target }
    val totalTercapai  = list.count { it.terkumpul >= it.target }
    val overallProg    = if (totalTarget > 0) (totalTerkumpul.toFloat() / totalTarget).coerceIn(0f, 1f) else 0f

    Column(modifier = Modifier.fillMaxSize().background(BgPage)) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(brush = HeaderGrad, shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .padding(horizontal = 20.dp).padding(top = 48.dp, bottom = 22.dp)
        ) {
            Column {
                Text("Statistik", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = White)
                Text("Ringkasan tabunganmu", fontSize = 12.sp, color = White.copy(0.72f))
            }
        }
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary cards
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(BgSurface)
                    .border(0.5.dp, Blue100, RoundedCornerShape(14.dp)).padding(14.dp)) {
                    Column {
                        Text("Total Terkumpul", fontSize = 11.sp, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (totalTerkumpul >= 1_000_000) "Rp ${String.format("%.1f", totalTerkumpul / 1_000_000f)}jt"
                            else "Rp ${"%,d".format(totalTerkumpul)}",
                            fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Blue700
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(BgSurface)
                    .border(0.5.dp, Blue100, RoundedCornerShape(14.dp)).padding(14.dp)) {
                    Column {
                        Text("Total Target", fontSize = 11.sp, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (totalTarget >= 1_000_000) "Rp ${String.format("%.1f", totalTarget / 1_000_000f)}jt"
                            else "Rp ${"%,d".format(totalTarget)}",
                            fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                        )
                    }
                }
            }
            // Progress keseluruhan
            CleanCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Progress Keseluruhan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("${(overallProg * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Blue500)
                    }
                    Spacer(Modifier.height(10.dp))
                    CleanProgressBar(overallProg, Modifier.fillMaxWidth())
                }
            }
            // Stats row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    Triple("$totalTrx", "transaksi", BgInput),
                    Triple("$totalAktif", "berjalan", Blue50),
                    Triple("$totalTercapai", "tercapai", GreenBg)
                ).forEach { (v, lbl, bg) ->
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(bg)
                        .border(0.5.dp, Blue100, RoundedCornerShape(12.dp)).padding(12.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(v, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                color = if (bg == GreenBg) GreenSoft else Blue700)
                            Text(lbl, fontSize = 10.sp,
                                color = if (bg == GreenBg) GreenSoft.copy(0.7f) else TextSecondary)
                        }
                    }
                }
            }
            // Per celengan
            if (list.isNotEmpty()) {
                CleanCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Progress Per Celengan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(Modifier.height(14.dp))
                        val barColors = listOf(Blue500, GreenSoft, OrangeSoft, RedSoft, Blue300)
                        list.forEachIndexed { i, cel ->
                            val p = if (cel.target > 0) (cel.terkumpul.toFloat() / cel.target).coerceIn(0f, 1f) else 0f
                            val col = barColors[i % barColors.size]
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(cel.nama, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Spacer(Modifier.width(8.dp))
                                Text("${(p * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = col)
                            }
                            Spacer(Modifier.height(5.dp))
                            Box(Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(20.dp)).background(Blue50)) {
                                val animP by animateFloatAsState(p, tween(700, i * 120), label = "sp$i")
                                Box(Modifier.fillMaxHeight().fillMaxWidth(animP).clip(RoundedCornerShape(20.dp)).background(col))
                            }
                            if (i < list.lastIndex) Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(BgSurface)
                    .border(0.5.dp, Blue100, RoundedCornerShape(14.dp)).padding(24.dp),
                    contentAlignment = Alignment.Center) {
                    Text("Belum ada celengan", fontSize = 14.sp, color = TextSecondary)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  AKTIVITAS SCREEN
// ═══════════════════════════════════════════════════════════════════

@Composable
fun AktivitasScreen(list: List<Celengan>) {
    // Gabungkan semua riwayat dari semua celengan, urutkan terbaru
    val semuaAktivitas = remember(list) {
        list.flatMap { cel ->
            cel.riwayat.map { trx -> Triple(cel.nama, trx, cel) }
        }.sortedByDescending { it.second.tanggal }
    }

    Column(modifier = Modifier.fillMaxSize().background(BgPage)) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(brush = HeaderGrad, shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .padding(horizontal = 20.dp).padding(top = 48.dp, bottom = 22.dp)
        ) {
            Column {
                Text("Aktivitas", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = White)
                Text("Semua aktivitas tabunganmu", fontSize = 12.sp, color = White.copy(0.72f))
            }
        }

        if (semuaAktivitas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().background(BgPage), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(BgSubtle), contentAlignment = Alignment.Center) {
                        Text("🔔", fontSize = 32.sp)
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("Belum ada aktivitas", fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("Mulai menabung untuk melihat aktivitas", fontSize = 13.sp, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(BgPage),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(semuaAktivitas) { (namaCelengan, trx, _) ->
                    val isMasuk = trx.tipe == "MASUK"
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(BgSurface)
                            .border(0.5.dp, if (isMasuk) Blue100 else Color(0xFFFFCDD2), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                                    .background(if (isMasuk) Blue50 else Color(0xFFFFEBEE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isMasuk) "🪙" else "💸", fontSize = 18.sp)
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (isMasuk) "Menabung — $namaCelengan" else "Pakai Tabungan — $namaCelengan",
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                                Text(trx.tanggal, fontSize = 11.sp, color = TextSecondary)
                            }
                            Text(
                                "${if (isMasuk) "+" else "-"}Rp ${"%,d".format(trx.nominal)}",
                                fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                color = if (isMasuk) GreenSoft else RedSoft
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
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
    var selectedTab    by remember { mutableStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val totalTarget     = list.sumOf { it.target }
    val totalTerkumpul  = list.sumOf { it.terkumpul }
    val overallProgress = if (totalTarget > 0) (totalTerkumpul.toFloat() / totalTarget).coerceIn(0f, 1f) else 0f
    val totalTercapai   = list.count { it.terkumpul >= it.target }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onTambah,
                containerColor = Blue500,
                contentColor   = White,
                shape          = CircleShape,
                modifier       = Modifier.size(58.dp)
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
                        verticalAlignment     = Alignment.CenterVertically
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
                                Text("wishPay", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = White)
                                Text("Halo, semangat nabung!", fontSize = 11.sp, color = White.copy(0.72f))
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
                                text      = "${list.size} Aktif",
                                bgColor   = White.copy(alpha = 0.20f),
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
                            Text("Progress keseluruhan", fontSize = 11.sp, color = White.copy(0.75f))
                            Text("${(overallProgress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = White)
                        }
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(White.copy(0.22f))
                        ) {
                            val animProg by animateFloatAsState(overallProgress, tween(700, easing = EaseOutCubic), label = "hprog")
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
                            fontSize   = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color      = if (selected) Blue600 else TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── LIST ──────────────────────────────────────────────────
            val filteredList = when (selectedTab) {
                0    -> list.filter { it.terkumpul < it.target }
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
                        ) { Image(
                            painter = painterResource(id = R.drawable.logo_wishpay),
                            contentDescription = null,
                            modifier = Modifier.size(50.dp)
                        ) }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            if (selectedTab == 0) "Belum ada tabungan" else "Belum ada yang tercapai",
                            fontSize   = 15.sp,
                            color      = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Tap tombol + untuk mulai menabung",
                            fontSize = 13.sp,
                            color    = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize().background(BgPage),
                    contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
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
            animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
            label = "bearBobLogout"
        )
        val ringAlpha by infiniteL.animateFloat(
            initialValue = 0.25f, targetValue = 0.85f,
            animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOutSine), RepeatMode.Reverse),
            label = "ringAlphaLogout"
        )
        val heartBeat by infiniteL.animateFloat(
            initialValue = 1f, targetValue = 1.22f,
            animationSpec = infiniteRepeatable(tween(700, easing = EaseInOutSine), RepeatMode.Reverse),
            label = "heartBeat"
        )
        val starAlpha1 by infiniteL.animateFloat(
            initialValue = 0.4f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
            label = "star1"
        )
        val starAlpha2 by infiniteL.animateFloat(
            initialValue = 0.4f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(700, delayMillis = 300, easing = EaseInOutSine), RepeatMode.Reverse),
            label = "star2"
        )
        val starAlpha3 by infiniteL.animateFloat(
            initialValue = 0.4f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1100, delayMillis = 150, easing = EaseInOutSine), RepeatMode.Reverse),
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
                                    .border(1.5.dp, Blue300.copy(alpha = ringAlpha * 0.35f), CircleShape)
                            )
                            // Ring tengah
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Blue100.copy(alpha = ringAlpha * 0.5f), CircleShape)
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
    val progress = if (item.target > 0) (item.terkumpul.toFloat() / item.target).coerceIn(0f, 1f) else 0f
    val sisa     = item.target - item.terkumpul
    val tercapai = item.terkumpul >= item.target

    val perHari = if (item.nominal > 0) when (item.jenis) {
        "Mingguan" -> item.nominal / 7
        "Bulanan"  -> item.nominal / 30
        else       -> item.nominal
    }.coerceAtLeast(1) else 0
    val hariLagi = if (perHari > 0) (sisa / perHari).coerceAtLeast(0) else 0

    Card(
        onClick    = onClick,
        shape      = RoundedCornerShape(16.dp),
        colors     = CardDefaults.cardColors(containerColor = BgSurface),
        elevation  = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier   = Modifier.fillMaxWidth()
    ) {
        // Foto impian (jika ada)
        if (!item.image.isNullOrEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                Image(
                    painter = rememberAsyncImagePainter(Uri.parse(item.image)),
                    contentDescription = null,
                    modifier     = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
                // gradient overlay tipis
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, TextPrimary.copy(0.25f))
                            )
                        )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Baris atas: icon + nama + progress circle
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (tercapai) GreenBg else Blue50),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (tercapai) "🏆" else "🐷", fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            item.nama,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextPrimary,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(3.dp))
                        Badge(
                            text      = item.jenis,
                            bgColor   = Blue50,
                            textColor = Blue600
                        )
                    }
                }
                // Mini progress circle
                Box(contentAlignment = Alignment.Center) {
                    CircularProgress(progress = progress, size = 48, strokeWidth = 5f)
                    Text(
                        "${(progress * 100).toInt()}%",
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (tercapai) GreenSoft else Blue500
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Nominal baris
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom
            ) {
                Column {
                    Text("Terkumpul", fontSize = 10.sp, color = TextHint)
                    Text(
                        "Rp ${"%,d".format(item.terkumpul)}",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (tercapai) GreenSoft else Blue500
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Target", fontSize = 10.sp, color = TextHint)
                    Text(
                        "Rp ${"%,d".format(item.target)}",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            CleanProgressBar(progress = progress, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(10.dp))

            // Footer
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                if (tercapai) {
                    Badge("Tercapai", GreenBg, GreenSoft)
                } else {
                    Badge(
                        text      = "Sisa $hariLagi hari",
                        bgColor   = Blue50,
                        textColor = Blue600
                    )
                }
                if (item.nominal > 0) {
                    Text(
                        "Rp ${"%,d".format(item.nominal)} / ${item.jenis.lowercase()}",
                        fontSize = 11.sp,
                        color    = TextSecondary
                    )
                }
            }
        }
    }
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
    var input    by remember { mutableStateOf("") }
    var coinList by remember { mutableStateOf(listOf<Int>()) }
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var nominalKeluar by remember { mutableStateOf(0) }
    var showSuksesDialog by remember { mutableStateOf(false) }
    var nominalSukses by remember { mutableStateOf(0) }
    var showTercapaiDialog by remember { mutableStateOf(false) }
    val context  = LocalContext.current
    val progress = if (celengan.target > 0) (celengan.terkumpul.toFloat() / celengan.target).coerceIn(0f, 1f) else 0f
    val tercapai = celengan.terkumpul >= celengan.target

    Box(Modifier.fillMaxSize().background(BgPage)) {
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
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(White.copy(0.18f))
                                .clickable { onKembali() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowBack, null, tint = White, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            celengan.nama,
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = White,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis,
                            modifier   = Modifier.weight(1f).padding(horizontal = 12.dp),
                            textAlign  = TextAlign.Center
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
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Circular progress besar
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgress(progress = progress, size = 80, strokeWidth = 8f)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${(progress * 100).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                                Text("tercapai", fontSize = 9.sp, color = White.copy(0.7f))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Terkumpul", fontSize = 11.sp, color = White.copy(0.72f))
                            Text(
                                "Rp ${"%,d".format(celengan.terkumpul)}",
                                fontSize   = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color      = White
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Target", fontSize = 10.sp, color = White.copy(0.6f))
                                    Text("Rp ${"%,d".format(celengan.target)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = White.copy(0.9f))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Sisa", fontSize = 10.sp, color = White.copy(0.6f))
                                    Text("Rp ${"%,d".format((celengan.target - celengan.terkumpul).coerceAtLeast(0))}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = White.copy(0.9f))
                                }
                            }
                        }
                    }
                }
            }

            // ── KONTEN SCROLL ─────────────────────────────────────────
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
                                    tint     = Blue500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                if (celengan.nominal > 0)
                                    "${(celengan.target - celengan.terkumpul).coerceAtLeast(0) / celengan.nominal.coerceAtLeast(1)} hari"
                                else "—",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = TextPrimary,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis,
                                textAlign  = TextAlign.Center
                            )
                            Text("Estimasi", fontSize = 10.sp, color = TextSecondary, textAlign = TextAlign.Center)
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
                                    tint     = OrangeSoft,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Rp ${"%,d".format(celengan.nominal)}",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = TextPrimary,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis,
                                textAlign  = TextAlign.Center
                            )
                            Text("Per ${celengan.jenis.lowercase()}", fontSize = 10.sp, color = TextSecondary, textAlign = TextAlign.Center)
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
                                    tint     = GreenSoft,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "${celengan.riwayat.size}x",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = TextPrimary,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis,
                                textAlign  = TextAlign.Center
                            )
                            Text("Transaksi", fontSize = 10.sp, color = TextSecondary, textAlign = TextAlign.Center)
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
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Blue50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Notifications, null, tint = Blue500, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Pengingat Menabung", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
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
                        Text("Isi Tabungan", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value         = input,
                            onValueChange = { input = it },
                            label         = { Text("Nominal (Rp)") },
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(12.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue500,
                                focusedLabelColor  = Blue500,
                                cursorColor        = Blue500
                            ),
                            prefix = { Text("Rp ", color = TextHint) }
                        )

                        Spacer(Modifier.height(10.dp))

                        // Quick chips
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("5.000" to 5000, "10.000" to 10000, "50.000" to 50000).forEach { (label, value) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Blue50)
                                        .border(1.dp, Blue100, RoundedCornerShape(8.dp))
                                        .clickable { input = value.toString() }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text("Rp $label", fontSize = 12.sp, color = Blue600, fontWeight = FontWeight.Medium)
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
                                        celengan.terkumpul += tambah
                                        onUpdate()
                                        val now = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
                                            .format(Date())
                                        celengan.riwayat.add(
                                            Transaksi(
                                                tanggal = now,
                                                nominal = tambah,
                                                tipe = "MASUK"
                                            )
                                        )
                                        input    = ""
                                        nominalSukses = tambah
                                        showSuksesDialog = true
                                        if (celengan.terkumpul >= celengan.target) {
                                            showTercapaiDialog = true
                                        }
                                        coinList = (1..16).toList()
                                        val mp = MediaPlayer.create(context, R.raw.coin)
                                        mp.setOnCompletionListener { it.release() }
                                        mp.start()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nabung Sekarang",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = White
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

                            Text("Riwayat Transaksi", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

                            Spacer(Modifier.height(12.dp))

                            // ===============================
                            // 💰 UANG MASUK
                            // ===============================
                            if (riwayatMasuk.isNotEmpty()) {

                                Text("Uang Masuk", color = GreenSoft, fontWeight = FontWeight.SemiBold)

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
                                            Text(trx.tanggal, fontSize = 10.sp, color = TextSecondary)
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

                                Text("Pengeluaran", color = RedSoft, fontWeight = FontWeight.SemiBold)

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
                                            Text(trx.tanggal, fontSize = 10.sp, color = TextSecondary)
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
                    Text("Kembali", color = Blue600, fontWeight = FontWeight.Medium, fontSize = 14.sp)
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
                animationSpec = infiniteRepeatable(tween(850, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "bluePulseScale"
            )
            val blueRingAlpha by infiniteBlue.animateFloat(
                initialValue = 0.25f,
                targetValue = 0.85f,
                animationSpec = infiniteRepeatable(tween(950, easing = EaseInOutSine), RepeatMode.Reverse),
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
                animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "pulseIcon"
            )
            val ringAlpha by infinitePulse.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
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
                                            listOf(Color(0xFFEF9A9A), Color(0xFFEF5350), Color(0xFFC62828))
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
                                Triple("Saldo saat ini", "Rp ${"%,d".format(celengan.terkumpul)}", false),
                                Triple("Saldo setelah", "Rp ${"%,d".format(sisaSaldo.coerceAtLeast(0))}", true),
                                Triple("Tanggal", java.text.SimpleDateFormat("dd MMM yyyy").format(java.util.Date()), false)
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
                                            listOf(Color(0xFFEF9A9A), Color(0xFFEF5350), Color(0xFFC62828))
                                        )
                                    )
                                    .clickable {
                                        celengan.terkumpul -= nominalKeluar
                                        onUpdate()

                                        val now = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
                                            .format(Date())
                                        celengan.riwayat.add(
                                            Transaksi(
                                                tanggal = now,
                                                nominal = nominalKeluar,
                                                tipe = "KELUAR"
                                            )
                                        )

                                        input = ""
                                        showDialog = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                                        size = androidx.compose.ui.geometry.Size(size.width - 8f, size.height - 8f),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(3f, cap = StrokeCap.Round)
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
                                        size = androidx.compose.ui.geometry.Size(size.width - 6f, size.height - 6f),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(2f, cap = StrokeCap.Round)
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
                                Text("✓", color = White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "iconPulse"
            )
            val ringAlphaT by infiniteT.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(tween(1100, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "ringAlphaT"
            )
            val dotAlpha1 by infiniteT.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(500, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "dot1"
            )
            val dotAlpha2 by infiniteT.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(500, delayMillis = 160, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "dot2"
            )
            val dotAlpha3 by infiniteT.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(500, delayMillis = 320, easing = EaseInOutSine), RepeatMode.Reverse),
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
                                        .border(1.5.dp, Blue200.copy(ringAlphaT * 0.4f), CircleShape)
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
                                    Triple("Terkumpul", "Rp ${"%,d".format(celengan.terkumpul)}", true),
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
                                    Text("100%", fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold, color = Blue500)
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

        // ── COIN RAIN — hanya koin emas 🪙 ────────────────────────────
        coinList.forEachIndexed { index, _ ->
            val offsetY = remember { Animatable(-120f) }
            val randomX = remember { (16..340).random() }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(index * 80L)
                offsetY.animateTo(
                    targetValue   = 1200f,
                    animationSpec = tween(2200, easing = EaseInCubic)
                )
            }
            Text(
                text  = "🪙",
                fontSize = (18 + (index % 4) * 3).sp,
                modifier = Modifier
                    .offset(x = randomX.dp, y = offsetY.value.dp)
                    .alpha(0.92f)
                    .align(Alignment.TopStart)
            )
        }
        LaunchedEffect(coinList) {
            if (coinList.isNotEmpty()) {
                kotlinx.coroutines.delay(2600)
                coinList = emptyList()
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
    var nama         by remember { mutableStateOf("") }
    var target       by remember { mutableStateOf("") }
    var nominal      by remember { mutableStateOf("") }
    var jenis        by remember { mutableStateOf("Harian") }
    var imageUri     by remember { mutableStateOf<Uri?>(null) }
    var notifAktif   by remember { mutableStateOf(false) }
    var jam          by remember { mutableStateOf("08:00") }
    var hariTerpilih by remember { mutableStateOf(setOf<String>()) }
    val context      = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = saveImageToInternalStorage(context, it)
        }
    }

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
                        Icon(Icons.Default.ArrowBack, null, tint = White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Buat Celengan Baru", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = White)
                        Text("Tentukan targetmu hari ini", fontSize = 11.sp, color = White.copy(0.72f))
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

            // Foto picker — dengan icon AutoAwesome yang unik & tidak standar
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
                                Text("Ganti Foto", fontSize = 12.sp, color = White, fontWeight = FontWeight.Medium)
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Icon AutoAwesome — tidak standar & menarik
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Blue100, Blue50)
                                        )
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        brush = Brush.linearGradient(listOf(Blue300, Blue100)),
                                        shape = RoundedCornerShape(18.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Blue500,
                                    modifier = Modifier.size(28.dp)
                                )

                            }
                            Spacer(Modifier.height(10.dp))
                            Text("Tambah Foto Impian", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Blue500)
                            Text("Ketuk untuk memilih gambar", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // Detail tabungan
            CleanCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Detail Tabungan", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value         = nama,
                        onValueChange = { nama = it },
                        label         = { Text("Nama Tabungan") },
                        placeholder   = { Text("mis. Beli HP Baru") },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            focusedLabelColor  = Blue500,
                            cursorColor        = Blue500
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value         = target,
                        onValueChange = { target = it },
                        label         = { Text("Target Tabungan") },
                        placeholder   = { Text("mis. 2000000") },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp),
                        prefix        = { Text("Rp ", color = TextHint) },
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            focusedLabelColor  = Blue500,
                            cursorColor        = Blue500
                        )
                    )
                }
            }

            // Rencana pengisian
            CleanCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Rencana Pengisian", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(Modifier.height(14.dp))

                    // Jenis tabs
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
                                    fontSize   = 12.sp,
                                    fontWeight = if (dipilih) FontWeight.SemiBold else FontWeight.Normal,
                                    color      = if (dipilih) White else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value         = nominal,
                        onValueChange = { nominal = it },
                        label         = { Text("Nominal $jenis") },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp),
                        prefix        = { Text("Rp ", color = TextHint) },
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            focusedLabelColor  = Blue500,
                            cursorColor        = Blue500
                        )
                    )
                }
            }

            // Notifikasi
            CleanCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Blue50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Notifications, null, tint = Blue500, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Aktifkan Pengingat", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Jadwalkan waktu nabung", fontSize = 11.sp, color = TextSecondary)
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

                            // Time picker
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
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Waktu Pengingat", fontSize = 11.sp, color = TextSecondary)
                                    Text(jam, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Blue500)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Blue50),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Edit, null, tint = Blue500, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            Text("Hari Pengingat", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(10.dp))

                            val listHari = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                            val fullHari = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")

                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                fullHari.forEachIndexed { i, hari ->
                                    val dipilih = hariTerpilih.contains(hari)
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (dipilih) Blue500 else BgSubtle)
                                            .border(if (!dipilih) 1.dp else 0.dp, Blue100, CircleShape)
                                            .clickable {
                                                hariTerpilih = if (dipilih) hariTerpilih - hari else hariTerpilih + hari
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            listHari[i],
                                            fontSize   = 10.sp,
                                            fontWeight = if (dipilih) FontWeight.SemiBold else FontWeight.Normal,
                                            color      = if (dipilih) White else TextSecondary
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
                        val t = target.toIntOrNull() ?: 0
                        val n = nominal.toIntOrNull() ?: 0
                        if (notifAktif && jam.isNotEmpty()) {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            scheduleNotification(context, jam, userId)
                        }
                        onSimpan(
                            Celengan(
                                nama,
                                t,
                                0,
                                imageUri?.toString(), // 🔥 INI PENTING
                                n,
                                jenis,
                                notifAktif,
                                jam,
                                hariTerpilih.toList()
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Buat Celengan",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (canSave) White else TextHint
                )
            }

            // Batal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                        )
                    )
                    .clickable { onKembali() },
                contentAlignment = Alignment.Center
            ) {
                Text("Batal", color = Blue600, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  EDIT SCREEN
// ═══════════════════════════════════════════════════════════════════

@Composable
fun EditScreen(
    celengan: Celengan,
    onSimpan: () -> Unit,
    onKembali: () -> Unit
) {
    var nama         by remember { mutableStateOf(celengan.nama) }
    var target       by remember { mutableStateOf(celengan.target.toString()) }
    var nominal      by remember { mutableStateOf(celengan.nominal.toString()) }
    var jenis        by remember { mutableStateOf(celengan.jenis) }
    var imageUri     by remember { mutableStateOf<Uri?>(
        if (!celengan.image.isNullOrEmpty()) Uri.parse(celengan.image) else null
    ) }
    var notifAktif   by remember { mutableStateOf(celengan.notifAktif) }
    var jam          by remember { mutableStateOf(celengan.jamNotif) }
    var hariTerpilih by remember { mutableStateOf(celengan.hariNotif.toSet()) }
    var showSimpanDialog by remember { mutableStateOf(false) }
    val context      = LocalContext.current

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
                        Icon(Icons.Default.ArrowBack, null, tint = White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Edit Celengan", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = White)
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
                                Text("Ganti Foto", fontSize = 12.sp, color = White, fontWeight = FontWeight.Medium)
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
                                Icon(Icons.Default.CameraAlt, null, tint = Blue500, modifier = Modifier.size(28.dp))
                            }
                            Spacer(Modifier.height(10.dp))
                            Text("Tambah Foto Impian", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Blue500)
                            Text("Ketuk untuk memilih gambar", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // Detail tabungan
            CleanCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Detail Tabungan", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value         = nama,
                        onValueChange = { nama = it },
                        label         = { Text("Nama Tabungan") },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            focusedLabelColor  = Blue500,
                            cursorColor        = Blue500
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value         = target,
                        onValueChange = { target = it },
                        label         = { Text("Target Tabungan") },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp),
                        prefix        = { Text("Rp ", color = TextHint) },
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            focusedLabelColor  = Blue500,
                            cursorColor        = Blue500
                        )
                    )
                }
            }

            // Rencana pengisian
            CleanCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Rencana Pengisian", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
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
                                    fontSize   = 12.sp,
                                    fontWeight = if (dipilih) FontWeight.SemiBold else FontWeight.Normal,
                                    color      = if (dipilih) White else TextSecondary
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value         = nominal,
                        onValueChange = { nominal = it },
                        label         = { Text("Nominal $jenis") },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp),
                        prefix        = { Text("Rp ", color = TextHint) },
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            focusedLabelColor  = Blue500,
                            cursorColor        = Blue500
                        )
                    )
                }
            }

            // Notifikasi
            CleanCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Blue50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Notifications, null, tint = Blue500, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Aktifkan Pengingat", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Jadwalkan waktu nabung", fontSize = 11.sp, color = TextSecondary)
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
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Waktu Pengingat", fontSize = 11.sp, color = TextSecondary)
                                    Text(jam, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Blue500)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Blue50),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Edit, null, tint = Blue500, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Text("Hari Pengingat", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(10.dp))
                            val listHari = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                            val fullHari = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                fullHari.forEachIndexed { i, hari ->
                                    val dipilih = hariTerpilih.contains(hari)
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (dipilih) Blue500 else BgSubtle)
                                            .border(if (!dipilih) 1.dp else 0.dp, Blue100, CircleShape)
                                            .clickable {
                                                hariTerpilih = if (dipilih) hariTerpilih - hari else hariTerpilih + hari
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            listHari[i],
                                            fontSize   = 10.sp,
                                            fontWeight = if (dipilih) FontWeight.SemiBold else FontWeight.Normal,
                                            color      = if (dipilih) White else TextSecondary
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
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (canSave) White else TextHint
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
                                    .border(1.5.dp, Blue300.copy(alpha = ringAlphaE * 0.35f), CircleShape)
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
                                Text(nama, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blue700)
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
                                Text("Batal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Blue600)
                            }

                            // Simpan
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(50.dp)
                                    .shadow(10.dp, RoundedCornerShape(14.dp),
                                        ambientColor = Blue500.copy(0.35f),
                                        spotColor = Blue700.copy(0.4f))
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(BtnGrad)
                                    .clickable {
                                        // Simpan semua perubahan ke objek celengan
                                        celengan.nama      = nama
                                        celengan.target    = target.toIntOrNull() ?: celengan.target
                                        celengan.nominal   = nominal.toIntOrNull() ?: celengan.nominal
                                        celengan.jenis     = jenis
                                        celengan.image     = imageUri?.toString()
                                        celengan.notifAktif = notifAktif
                                        celengan.jamNotif  = jam
                                        celengan.hariNotif = hariTerpilih.toList()

                                        if (notifAktif) {
                                            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
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
                                    Text("✓", color = White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Simpan!", color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
    val auth        = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val email       = currentUser?.email ?: "user@wishpay.app"

    var displayName by remember {
        mutableStateOf(
            currentUser?.displayName?.ifEmpty { null } ?: email.substringBefore("@")
        )
    }

    val joinedDate = remember {
        val ts = currentUser?.metadata?.creationTimestamp ?: 0L
        if (ts > 0L) java.text.SimpleDateFormat("MMMM yyyy", Locale("id"))
            .format(java.util.Date(ts)) else "—"
    }

    var selectedTab      by remember { mutableStateOf(0) }
    var showEditName     by remember { mutableStateOf(false) }
    var showPassword     by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var editNameValue    by remember { mutableStateOf(displayName) }
    var newPassword      by remember { mutableStateOf("") }
    var confirmPassword  by remember { mutableStateOf("") }
    var showPwText       by remember { mutableStateOf(false) }
    var isLoading        by remember { mutableStateOf(false) }
    val context          = LocalContext.current


    var profileImageUri  by remember { mutableStateOf<Uri?>(null) }
    val userId2 = currentUser?.uid ?: ""

// Load foto profil dari Firestore
    LaunchedEffect(userId2) {
        if (userId2.isNotEmpty()) {
            val saved = FirestoreManager.loadProfileImage(userId2)
            if (!saved.isNullOrEmpty()) {
                profileImageUri = Uri.parse(saved)
            }
        }
    }

    val profileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val saved = saveImageToInternalStorage(context, it)
            profileImageUri = saved
            // Simpan ke Firestore
            kotlinx.coroutines.MainScope().launch {
                FirestoreManager.saveProfileImage(userId2, saved.toString())
            }
        }
    }

    // ── Stats ─────────────────────────────────────────────────────
    val totalTerkumpul = listCelengan.sumOf { it.terkumpul }
    val totalTarget    = listCelengan.sumOf { it.target }
    val totalAktif     = listCelengan.count { it.terkumpul < it.target }
    val totalTercapai  = listCelengan.count { it.terkumpul >= it.target }
    val overallProg    = if (totalTarget > 0) (totalTerkumpul.toFloat() / totalTarget).coerceIn(0f, 1f) else 0f
    val totalTrx       = listCelengan.sumOf { it.riwayat.size }
    val notifCount     = listCelengan.count { it.notifAktif }

    // ── Badges ────────────────────────────────────────────────────
    val badges = listOf(
        BadgeData("⭐", "Pemula",    "Buat celengan pertama",          listCelengan.isNotEmpty()),
        BadgeData("🔥", "Rajin",     "7 transaksi (${totalTrx}/7)",    totalTrx >= 7),
        BadgeData("💎", "Konsisten", "30 transaksi (${totalTrx}/30)",  totalTrx >= 30),
        BadgeData("🏆", "Pemenang",  "Target tercapai",                totalTercapai >= 1),
        BadgeData("🪙", "Kolektor",  "5 celengan (${listCelengan.size}/5)", listCelengan.size >= 5),
        BadgeData("👑", "Sultan",    "Rp 10jt total",                  totalTerkumpul >= 10_000_000)
    )
    val badgeEarned = badges.count { it.earned }

    // ── Animations ────────────────────────────────────────────────
    val infinite  = rememberInfiniteTransition(label = "profil")
    val bearBob   by infinite.animateFloat(0f, -8f,
        infiniteRepeatable(tween(2500, easing = EaseInOutSine), RepeatMode.Reverse), label = "pBear")
    val ringAlpha by infinite.animateFloat(0.15f, 0.55f,
        infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "pRing")

    Box(Modifier.fillMaxSize().background(BgPage)) {

        Column(Modifier.fillMaxSize()) {

            // ── HEADER ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = HeaderGrad, shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .padding(horizontal = 20.dp)
                    .padding(top = 48.dp, bottom = 36.dp)
            ) {
                Column {
                    // Nav bar
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                                .background(White.copy(0.18f)).clickable { onKembali() },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.ArrowBack, null, tint = White, modifier = Modifier.size(18.dp)) }
                        Text("profil saya", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = White.copy(0.92f))
                        Spacer(Modifier.size(40.dp))
                    }

                    Spacer(Modifier.height(20.dp))

                    // Avatar + info
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                            Box(Modifier.size(108.dp).clip(CircleShape).border(2.dp, White.copy(ringAlpha * 0.4f), CircleShape))
                            Box(Modifier.size(88.dp).clip(CircleShape).border(1.5.dp, White.copy(ringAlpha * 0.25f), CircleShape))
                            Box(
                                modifier = Modifier
                                    .size(74.dp)
                                    .clip(CircleShape)
                                    .background(Blue100)
                                    .border(3.dp, White.copy(0.78f), CircleShape)
                                    .clickable { profileLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (profileImageUri != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(profileImageUri),
                                        contentDescription = "foto profil",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    // overlay kamera kecil
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .align(Alignment.BottomEnd)
                                            .clip(CircleShape)
                                            .background(White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = Blue500,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                } else {
                                    Text("🐻", fontSize = 36.sp, modifier = Modifier.offset(y = bearBob.dp))
                                    // overlay kamera kecil
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .align(Alignment.BottomEnd)
                                            .clip(CircleShape)
                                            .background(White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = Blue500,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(displayName, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = White)
                        Spacer(Modifier.height(2.dp))
                        Text(email, fontSize = 11.sp, color = White.copy(0.68f))
                        Spacer(Modifier.height(9.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            Badge("$totalAktif aktif",    White.copy(0.17f), White.copy(0.9f))
                            Badge("$totalTercapai tercapai", White.copy(0.17f), White.copy(0.9f))
                        }
                    }
                }
            }

            // ── SCROLLABLE CONTENT ────────────────────────────────
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                Spacer(Modifier.height(14.dp))

                // Tab bar
                Row(
                    modifier = Modifier.padding(horizontal = 13.dp)
                        .clip(RoundedCornerShape(10.dp)).background(BgSurface)
                        .border(0.5.dp, Blue200, RoundedCornerShape(10.dp)).padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    listOf("profil", "statistik", "pencapaian").forEachIndexed { i, lbl ->
                        val sel = selectedTab == i
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(7.dp))
                                .background(if (sel) Blue700 else Color.Transparent)
                                .clickable { selectedTab = i }.padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(lbl, fontSize = 11.sp,
                                fontWeight = if (sel) FontWeight.Medium else FontWeight.Normal,
                                color = if (sel) White else TextSecondary)
                        }
                    }
                }

                Spacer(Modifier.height(11.dp))

                // ── TAB: PROFIL ───────────────────────────────────
                if (selectedTab == 0) {
                    Column(Modifier.padding(horizontal = 13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

                        // Kartu: Akun Saya
                        Card(
                            shape  = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(BgSurface),
                            border = BorderStroke(0.5.dp, Blue200),
                            elevation = CardDefaults.cardElevation(0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("akun saya", fontSize = 10.sp, color = Blue600,
                                    fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp,
                                    modifier = Modifier.padding(start = 14.dp, top = 10.dp, bottom = 7.dp))

                                // Nama tampilan
                                HorizontalDivider(color = Blue50)
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { showEditName = true }
                                        .padding(horizontal = 14.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(Blue50), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Edit, null, tint = Blue600, modifier = Modifier.size(15.dp))
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text("nama tampilan", fontSize = 10.sp, color = TextHint)
                                        Text(displayName, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                    }
                                    Icon(Icons.Default.ArrowBack, null, tint = Blue200, modifier = Modifier.size(13.dp).rotate(180f))
                                }

                                // Email
                                HorizontalDivider(color = Blue50)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(Blue50), contentAlignment = Alignment.Center) {
                                        Text("✉", fontSize = 14.sp)
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text("email", fontSize = 10.sp, color = TextHint)
                                        Text(email, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium,
                                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }

                                // Password
                                HorizontalDivider(color = Blue50)
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { showPassword = true }
                                        .padding(horizontal = 14.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(Blue50), contentAlignment = Alignment.Center) {
                                        Text("🔒", fontSize = 14.sp)
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text("password", fontSize = 10.sp, color = TextHint)
                                        Text("••••••••", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                    }
                                    Icon(Icons.Default.ArrowBack, null, tint = Blue200, modifier = Modifier.size(13.dp).rotate(180f))
                                }
                            }
                        }

                        // Kartu: Pengaturan
                        Card(
                            shape  = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(BgSurface),
                            border = BorderStroke(0.5.dp, Blue200),
                            elevation = CardDefaults.cardElevation(0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("pengaturan", fontSize = 10.sp, color = Blue600,
                                    fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp,
                                    modifier = Modifier.padding(start = 14.dp, top = 10.dp, bottom = 7.dp))

                                // Notifikasi
                                HorizontalDivider(color = Blue50)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(GreenBg), contentAlignment = Alignment.Center) {
                                        Text("🔔", fontSize = 14.sp)
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text("notifikasi harian", fontSize = 10.sp, color = TextHint)
                                        Text(
                                            if (notifCount > 0) "$notifCount celengan aktif" else "tidak aktif",
                                            fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium
                                        )
                                    }
                                    // Toggle visual
                                    Box(
                                        modifier = Modifier.width(38.dp).height(22.dp)
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(if (notifCount > 0) Blue500 else Blue100)
                                            .padding(3.dp),
                                        contentAlignment = if (notifCount > 0) Alignment.CenterEnd else Alignment.CenterStart
                                    ) {
                                        Box(Modifier.size(16.dp).clip(CircleShape).background(White))
                                    }
                                }

                                // Bergabung sejak
                                HorizontalDivider(color = Blue50)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(Blue50), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.CalendarToday, null, tint = Blue500, modifier = Modifier.size(15.dp))
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text("bergabung sejak", fontSize = 10.sp, color = TextHint)
                                        Text(joinedDate, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        // Kartu: Logout
                        Card(
                            shape  = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(BgSurface),
                            border = BorderStroke(0.5.dp, Color(0xFFFFCDD2)),
                            elevation = CardDefaults.cardElevation(0.dp),
                            modifier = Modifier.fillMaxWidth().clickable { showLogoutDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(Color(0xFFFFEBEE)), contentAlignment = Alignment.Center) {
                                    Text("🚪", fontSize = 14.sp)
                                }
                                Text("logout", fontSize = 13.sp, color = RedSoft, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowBack, null, tint = Color(0xFFFFCDD2), modifier = Modifier.size(13.dp).rotate(180f))
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }

                // ── TAB: STATISTIK ────────────────────────────────
                if (selectedTab == 1) {
                    Column(Modifier.padding(horizontal = 13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

                        // Ringkasan
                        Card(
                            shape  = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(BgSurface),
                            border = BorderStroke(0.5.dp, Blue200),
                            elevation = CardDefaults.cardElevation(0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(13.dp)) {
                                Text("ringkasan tabungan", fontSize = 10.sp, color = Blue600,
                                    fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp)
                                Spacer(Modifier.height(11.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Blue50).padding(10.dp)) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                if (totalTerkumpul >= 1_000_000) "Rp ${String.format("%.1f", totalTerkumpul / 1_000_000f)}jt"
                                                else "Rp ${"%,d".format(totalTerkumpul)}",
                                                fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Blue800
                                            )
                                            Text("terkumpul", fontSize = 10.sp, color = Blue500)
                                        }
                                    }
                                    Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(GreenBg).padding(10.dp)) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                if (totalTarget >= 1_000_000) "Rp ${String.format("%.1f", totalTarget / 1_000_000f)}jt"
                                                else "Rp ${"%,d".format(totalTarget)}",
                                                fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF27500A)
                                            )
                                            Text("total target", fontSize = 10.sp, color = Color(0xFF3B6D11))
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("progress keseluruhan", fontSize = 10.sp, color = TextSecondary)
                                    Text("${(overallProg * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Blue600)
                                }
                                Spacer(Modifier.height(5.dp))
                                CleanProgressBar(overallProg, Modifier.fillMaxWidth())
                            }
                        }

                        // Per celengan
                        if (listCelengan.isNotEmpty()) {
                            Card(
                                shape  = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(BgSurface),
                                border = BorderStroke(0.5.dp, Blue200),
                                elevation = CardDefaults.cardElevation(0.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(13.dp)) {
                                    Text("per celengan", fontSize = 10.sp, color = Blue600,
                                        fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp)
                                    Spacer(Modifier.height(11.dp))
                                    val barColors = listOf(Blue500, GreenSoft, OrangeSoft, RedSoft, Blue300)
                                    listCelengan.take(5).forEachIndexed { i, cel ->
                                        val p   = if (cel.target > 0) (cel.terkumpul.toFloat() / cel.target).coerceIn(0f, 1f) else 0f
                                        val col = barColors[i % barColors.size]
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(cel.nama, fontSize = 10.sp, color = TextSecondary,
                                                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                            Spacer(Modifier.width(8.dp))
                                            Text("${(p * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = col)
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(20.dp)).background(Blue50)) {
                                            val animP by animateFloatAsState(p, tween(700, i * 120), label = "sp$i")
                                            Box(Modifier.fillMaxHeight().fillMaxWidth(animP).clip(RoundedCornerShape(20.dp)).background(col))
                                        }
                                        if (i < listCelengan.take(5).lastIndex) Spacer(Modifier.height(10.dp))
                                    }
                                }
                            }
                        }

                        // Statistik umum
                        Card(
                            shape  = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(BgSurface),
                            border = BorderStroke(0.5.dp, Blue200),
                            elevation = CardDefaults.cardElevation(0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(13.dp)) {
                                Text("statistik umum", fontSize = 10.sp, color = Blue600,
                                    fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp)
                                Spacer(Modifier.height(11.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(
                                        Triple("$totalTrx",     "transaksi",  BgInput),
                                        Triple("$totalAktif",   "berjalan",   BgInput),
                                        Triple("$totalTercapai","tercapai",   GreenBg)
                                    ).forEach { (v, lbl, bg) ->
                                        Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(bg).padding(10.dp)) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                                Text(v,   fontSize = 16.sp, fontWeight = FontWeight.Medium,
                                                    color = if (bg == GreenBg) GreenSoft else Blue700)
                                                Text(lbl, fontSize = 10.sp,
                                                    color = if (bg == GreenBg) GreenSoft.copy(0.7f) else TextSecondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }

                // ── TAB: PENCAPAIAN ───────────────────────────────
                if (selectedTab == 2) {
                    Column(Modifier.padding(horizontal = 13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

                        Card(
                            shape  = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(BgSurface),
                            border = BorderStroke(0.5.dp, Blue200),
                            elevation = CardDefaults.cardElevation(0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(13.dp)) {
                                Text("pencapaianmu", fontSize = 10.sp, color = Blue600,
                                    fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp)
                                Spacer(Modifier.height(11.dp))
                                badges.chunked(2).forEachIndexed { rowIdx, rowBadges ->
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                        rowBadges.forEach { badge ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (badge.earned) Blue50 else Color(0xFFF5F5F5))
                                                    .border(0.5.dp,
                                                        if (badge.earned) Blue200 else Color(0xFFE0E0E0),
                                                        RoundedCornerShape(10.dp))
                                                    .padding(11.dp)
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                                    Text(badge.icon, fontSize = 20.sp,
                                                        color = if (badge.earned) Blue700 else Color(0xFFB0BEC5))
                                                    Spacer(Modifier.height(4.dp))
                                                    Text(badge.label, fontSize = 11.sp, fontWeight = FontWeight.Medium,
                                                        color = if (badge.earned) Blue800 else Color(0xFF90A4AE))
                                                    Text(badge.desc, fontSize = 9.sp, textAlign = TextAlign.Center,
                                                        color = if (badge.earned) Blue500 else Color(0xFFB0BEC5))
                                                    Spacer(Modifier.height(4.dp))
                                                    Text(
                                                        if (badge.earned) "✓ terbuka" else "terkunci",
                                                        fontSize = 9.sp,
                                                        color = if (badge.earned) GreenSoft else Color(0xFFBDBDBD)
                                                    )
                                                }
                                            }
                                        }
                                        if (rowBadges.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                    if (rowIdx < badges.chunked(2).lastIndex) Spacer(Modifier.height(9.dp))
                                }
                            }
                        }

                        // Progress pencapaian
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Blue50).padding(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("$badgeEarned / ${badges.size}", fontSize = 19.sp, fontWeight = FontWeight.Medium, color = Blue800)
                                Text("pencapaian terbuka", fontSize = 11.sp, color = Blue500)
                                Spacer(Modifier.height(8.dp))
                                CleanProgressBar(badgeEarned.toFloat() / badges.size, Modifier.fillMaxWidth())
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }

        // ── POPUP: Edit Nama ──────────────────────────────────────────
        if (showEditName) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(Color(0xFF0A1628).copy(0.72f))
                    .clickable { showEditName = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(BgSurface),
                    modifier = Modifier.padding(24.dp).fillMaxWidth().clickable(enabled = false) {}
                ) {
                    Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✏️ Edit Nama", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Blue700)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = editNameValue, onValueChange = { editNameValue = it },
                            label = { Text("Nama Tampilan") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Blue500, focusedLabelColor = Blue500, cursorColor = Blue500)
                        )
                        Spacer(Modifier.height(18.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(12.dp))
                                    .background(Blue50).border(1.dp, Blue100, RoundedCornerShape(12.dp))
                                    .clickable { showEditName = false },
                                contentAlignment = Alignment.Center
                            ) { Text("Batal", fontSize = 13.sp, color = Blue600, fontWeight = FontWeight.SemiBold) }
                            Box(
                                modifier = Modifier.weight(1.5f).height(48.dp).clip(RoundedCornerShape(12.dp))
                                    .background(BtnGrad).clickable {
                                        if (editNameValue.isNotEmpty()) {
                                            isLoading = true
                                            val updates = com.google.firebase.auth.UserProfileChangeRequest
                                                .Builder().setDisplayName(editNameValue).build()
                                            currentUser?.updateProfile(updates)
                                                ?.addOnSuccessListener {
                                                    isLoading = false
                                                    displayName = editNameValue
                                                    showEditName = false
                                                    Toast.makeText(context, "Nama berhasil diubah 🎉", Toast.LENGTH_SHORT).show()
                                                }
                                                ?.addOnFailureListener {
                                                    isLoading = false
                                                    Toast.makeText(context, "Gagal ubah nama 😢", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                else Text("Simpan ✓", fontSize = 13.sp, color = White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ── POPUP: Ganti Password ─────────────────────────────────────
        if (showPassword) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(Color(0xFF0A1628).copy(0.72f))
                    .clickable { showPassword = false; newPassword = ""; confirmPassword = "" },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(BgSurface),
                    modifier = Modifier.padding(24.dp).fillMaxWidth().clickable(enabled = false) {}
                ) {
                    Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔒 Ganti Password", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Blue700)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newPassword, onValueChange = { newPassword = it },
                            label = { Text("Password Baru") },
                            visualTransformation = if (showPwText) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPwText = !showPwText }) {
                                    Icon(Icons.Default.Visibility, null,
                                        tint = if (showPwText) Blue500 else Blue200, modifier = Modifier.size(18.dp))
                                }
                            },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Blue500, focusedLabelColor = Blue500, cursorColor = Blue500)
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = confirmPassword, onValueChange = { confirmPassword = it },
                            label = { Text("Konfirmasi Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Blue500, focusedLabelColor = Blue500, cursorColor = Blue500)
                        )
                        Spacer(Modifier.height(18.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(12.dp))
                                    .background(Blue50).border(1.dp, Blue100, RoundedCornerShape(12.dp))
                                    .clickable { showPassword = false; newPassword = ""; confirmPassword = "" },
                                contentAlignment = Alignment.Center
                            ) { Text("Batal", fontSize = 13.sp, color = Blue600, fontWeight = FontWeight.SemiBold) }
                            Box(
                                modifier = Modifier.weight(1.5f).height(48.dp).clip(RoundedCornerShape(12.dp))
                                    .background(BtnGrad).clickable {
                                        when {
                                            newPassword.length < 6 ->
                                                Toast.makeText(context, "Min 6 karakter 🔑", Toast.LENGTH_SHORT).show()
                                            newPassword != confirmPassword ->
                                                Toast.makeText(context, "Password tidak sama 😢", Toast.LENGTH_SHORT).show()
                                            else -> {
                                                isLoading = true
                                                currentUser?.updatePassword(newPassword)
                                                    ?.addOnSuccessListener {
                                                        isLoading = false
                                                        newPassword = ""; confirmPassword = ""
                                                        showPassword = false
                                                        Toast.makeText(context, "Password berhasil diubah 🎉", Toast.LENGTH_SHORT).show()
                                                    }
                                                    ?.addOnFailureListener {
                                                        isLoading = false
                                                        Toast.makeText(context, "Gagal. Coba re-login dulu 😢", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                else Text("Simpan ✓", fontSize = 13.sp, color = White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ── POPUP: Logout ─────────────────────────────────────────────
        if (showLogoutDialog) {
            val popupScale by animateFloatAsState(
                if (showLogoutDialog) 1f else 0.85f,
                spring(dampingRatio = 0.55f, stiffness = 400f), label = "logoutScaleP"
            )
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(Color(0xFF0A1628).copy(0.72f))
                    .clickable { showLogoutDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.padding(28.dp).scale(popupScale)
                        .shadow(32.dp, RoundedCornerShape(28.dp),
                            ambientColor = Blue500.copy(0.2f), spotColor = Blue700.copy(0.25f))
                        .clip(RoundedCornerShape(28.dp)).background(White)
                        .border(2.dp, Blue100, RoundedCornerShape(28.dp))
                        .clickable(enabled = false) {}
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.fillMaxWidth().height(5.dp).background(BtnGrad))
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(22.dp)) {
                            Text("🐻", fontSize = 42.sp)
                            Spacer(Modifier.height(10.dp))
                            Text("Mau pergi dulu? 🥺", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                color = Blue700, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(4.dp))
                            Text("Beruang akan kangen nunggu kamu balik~",
                                fontSize = 12.sp, color = Blue400, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(20.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(14.dp))
                                        .background(Blue50).border(1.5.dp, Blue100, RoundedCornerShape(14.dp))
                                        .clickable { showLogoutDialog = false },
                                    contentAlignment = Alignment.Center
                                ) { Text("Batal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Blue600) }
                                Box(
                                    modifier = Modifier.weight(1.5f).height(50.dp)
                                        .shadow(10.dp, RoundedCornerShape(14.dp),
                                            ambientColor = Blue500.copy(0.35f), spotColor = Blue700.copy(0.4f))
                                        .clip(RoundedCornerShape(14.dp)).background(BtnGrad)
                                        .clickable { showLogoutDialog = false; onLogout() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("👋", fontSize = 15.sp)
                                        Text("Ya, Dadah!", color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
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