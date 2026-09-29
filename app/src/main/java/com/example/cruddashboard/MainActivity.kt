package com.example.cruddashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cruddashboard.data.CustomerRepository
import com.example.cruddashboard.model.AdminConfig
import com.example.cruddashboard.model.Customer
import com.example.cruddashboard.model.CustomerStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Indigo = Color(0xFF5B5FEF)
private val Ink = Color(0xFF171A2B)
private val Muted = Color(0xFF6F7387)
private val Canvas = Color(0xFFF7F8FC)
private val Mint = Color(0xFF17A673)
private val Amber = Color(0xFFE69718)
private val Coral = Color(0xFFE75D68)

private val AppColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    secondary = Mint,
    background = Canvas,
    surface = Color.White,
    onSurface = Ink,
    onSurfaceVariant = Muted,
    outline = Color(0xFFDADDE8),
    error = Coral
)

private enum class AuthScreen { LOGIN, SIGN_UP, FORGOT_PASSWORD }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = AppColors) {
                val context = LocalContext.current.applicationContext
                val controller = remember { AppController(CustomerRepository(context)) }
                var showSignUp by remember { mutableStateOf(false) }
                var showAdminConfig by remember { mutableStateOf(false) }
                var authScreen by remember { mutableStateOf(AuthScreen.LOGIN) }
                Surface(modifier = Modifier.fillMaxSize(), color = Canvas) {
                    when {
                        controller.isLoggedIn -> DashboardScreen(controller)
                        authScreen == AuthScreen.SIGN_UP -> SignUpScreen(controller, onSignIn = { authScreen = AuthScreen.LOGIN })
                        authScreen == AuthScreen.FORGOT_PASSWORD -> ForgotPasswordScreen(controller, onSignIn = { authScreen = AuthScreen.LOGIN })
                        showAdminConfig -> AdminConfigScreen(
                            initialConfig = controller.loadAdminConfig(),
                            onBack = { showAdminConfig = false },
                            onSave = { baseUrl, adminName, adminPassword ->
                                controller.saveAdminConfig(baseUrl, adminName, adminPassword).also { error ->
                                    if (error == null) showAdminConfig = false
                                }
                            }
                        )
                        showSignUp -> SignUpScreen(controller, onSignIn = { showSignUp = false })
                        else -> LoginScreen(
                            controller,
                            onSignUp = { authScreen = AuthScreen.SIGN_UP },
                            onAdminConfig = { showAdminConfig = true },
                            onForgotPassword = { authScreen = AuthScreen.FORGOT_PASSWORD }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginScreen(controller: AppController, onSignUp: () -> Unit, onForgotPassword: () -> Unit,onAdminConfig: () -> Unit) {
    var email by remember { mutableStateOf("admin@cliently.app") }
    var password by remember { mutableStateOf("password") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF0F1FF), Canvas, Color.White)))
            .statusBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Indigo)
                    .combinedClickable(onClick = {}, onLongClick = onAdminConfig),
                contentAlignment = Alignment.Center
            ) {
                Text("C", color = Color.White, fontWeight = FontWeight.Black, fontSize = 34.sp)
            }
            Spacer(Modifier.height(24.dp))
            Text("Welcome back", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Ink)
            Text("Sign in to manage your customers", color = Muted, modifier = Modifier.padding(top = 7.dp, bottom = 30.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = email, onValueChange = { email = it; error = null },
                        label = { Text("Email address") }, leadingIcon = { Icon(Icons.Default.Email, null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = password, onValueChange = { password = it; error = null },
                        label = { Text("Password") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, if (passwordVisible) "Hide password" else "Show password")
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus(); error = controller.login(email, password)
                        }), shape = RoundedCornerShape(14.dp), isError = error != null
                    )
                    TextButton(onClick = onForgotPassword, modifier = Modifier.align(Alignment.End)) {
                        Text("Forgot password?")
                    }
                    if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Button(
                        onClick = { focusManager.clearFocus(); error = controller.login(email, password) },
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp)
                    ) { Text("Sign in", fontWeight = FontWeight.Bold) }
                }
            }
            TextButton(onClick = onSignUp, modifier = Modifier.padding(top = 10.dp)) {
                Text("Don't have an account? Sign up")
            }
            Text("Demo: use any valid email and a 6+ character password", color = Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 20.dp))
        }
    }
}

@Composable
private fun ForgotPasswordScreen(controller: AppController, onSignIn: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var passwordChanged by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        error = controller.resetPassword(email, newPassword, confirmPassword)
        passwordChanged = error == null
    }

    BackHandler(onBack = onSignIn)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF0F1FF), Canvas, Color.White)))
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (passwordChanged) {
            Icon(Icons.Default.CheckCircle, null, tint = Mint, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(20.dp))
            Text("Password changed", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Ink)
            Text("You can now sign in with your new password.", color = Muted, modifier = Modifier.padding(top = 7.dp, bottom = 24.dp))
            Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp)) {
                Text("Back to sign in", fontWeight = FontWeight.Bold)
            }
        } else {
            Text("Reset password", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Ink)
            Text("Enter your email and choose a new password", color = Muted, modifier = Modifier.padding(top = 7.dp, bottom = 30.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = email, onValueChange = { email = it; error = null },
                        label = { Text("Email address") }, leadingIcon = { Icon(Icons.Default.Email, null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = newPassword, onValueChange = { newPassword = it; error = null },
                        label = { Text("New password") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, if (passwordVisible) "Hide password" else "Show password")
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = confirmPassword, onValueChange = { confirmPassword = it; error = null },
                        label = { Text("Confirm password") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        shape = RoundedCornerShape(14.dp), isError = error != null
                    )
                    if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = submit, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp)) {
                        Text("Change password", fontWeight = FontWeight.Bold)
                    }
                }
            }
            TextButton(onClick = onSignIn, modifier = Modifier.padding(top = 10.dp)) {
                Text("Back to sign in")
            }
        }
    }
}

@Composable
private fun AdminConfigScreen(
    initialConfig: AdminConfig,
    onBack: () -> Unit,
    onSave: (String, String, String) -> String?
) {
    var baseUrl by remember(initialConfig) { mutableStateOf(initialConfig.baseUrl) }
    var adminName by remember(initialConfig) { mutableStateOf(initialConfig.adminName) }
    var adminPassword by remember(initialConfig) { mutableStateOf(initialConfig.adminPassword) }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        error = onSave(baseUrl, adminName, adminPassword)
    }

    BackHandler(onBack = onBack)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF0F1FF), Canvas, Color.White)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
            Column(Modifier.padding(start = 4.dp)) {
                Text("Admin configuration", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Ink)
                Text("Configure this app installation", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(28.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it; error = null },
                    label = { Text("Base URL") },
                    leadingIcon = { Icon(Icons.Default.Language, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(14.dp),
                    isError = error != null && baseUrl.isBlank()
                )
                OutlinedTextField(
                    value = adminName,
                    onValueChange = { adminName = it; error = null },
                    label = { Text("Admin name") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(14.dp),
                    isError = error != null && adminName.isBlank()
                )
                OutlinedTextField(
                    value = adminPassword,
                    onValueChange = { adminPassword = it; error = null },
                    label = { Text("Admin password") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    shape = RoundedCornerShape(14.dp),
                    isError = error != null && adminPassword.isBlank()
                )
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = submit,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Settings, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save configuration", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SignUpScreen(controller: AppController, onSignIn: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        error = controller.signUp(username, email, password)
    }

    BackHandler(onBack = onSignIn)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF0F1FF), Canvas, Color.White)))
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create account", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Ink)
        Text("Sign up to start managing customers", color = Muted, modifier = Modifier.padding(top = 7.dp, bottom = 30.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = username, onValueChange = { username = it; error = null },
                    label = { Text("Username") }, leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(14.dp), isError = error != null && username.isBlank()
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it; error = null },
                    label = { Text("Email address") }, leadingIcon = { Icon(Icons.Default.Email, null) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(14.dp), isError = error != null && email.isBlank()
                )
                OutlinedTextField(
                    value = password, onValueChange = { password = it; error = null },
                    label = { Text("Password") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, if (passwordVisible) "Hide password" else "Show password")
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    shape = RoundedCornerShape(14.dp), isError = error != null && password.isBlank()
                )
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = submit,
                    modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp)
                ) { Text("Sign up", fontWeight = FontWeight.Bold) }
            }
        }
        TextButton(onClick = onSignIn, modifier = Modifier.padding(top = 10.dp)) {
            Text("Already have an account? Sign in")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreen(controller: AppController) {
    var editing by remember { mutableStateOf<Customer?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Customer?>(null) }
    var confirmLogout by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Canvas,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editing = null; showForm = true },
                modifier = Modifier.navigationBarsPadding(),
                containerColor = Indigo,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Add customer")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { DashboardHeader(onLogout = { confirmLogout = true }) }
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard("Total", controller.customers.size.toString(), Icons.Default.Group, Indigo, Modifier.weight(1f))
                        StatCard("Active", controller.customers.count { it.status == CustomerStatus.ACTIVE }.toString(), Icons.Default.CheckCircle, Mint, Modifier.weight(1f))
                        StatCard("Leads", controller.customers.count { it.status == CustomerStatus.LEAD }.toString(), Icons.Default.Person, Amber, Modifier.weight(1f))
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Customers", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            Text("${controller.filteredCustomers.size} records", color = Muted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = controller.searchQuery, onValueChange = { controller.searchQuery = it },
                        placeholder = { Text("Search name, company or status") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = if (controller.searchQuery.isNotEmpty()) {{ IconButton(onClick = { controller.searchQuery = "" }) { Icon(Icons.Default.Close, "Clear") } }} else null,
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
                    )
                }
            }
            if (controller.filteredCustomers.isEmpty()) {
                item { EmptyCustomers(hasSearch = controller.searchQuery.isNotBlank(), onAdd = { editing = null; showForm = true }) }
            } else {
                items(controller.filteredCustomers, key = { it.id }) { customer ->
                    CustomerCard(
                        customer = customer,
                        onEdit = { editing = customer; showForm = true },
                        onDelete = { deleting = customer },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }

    if (showForm) {
        CustomerFormSheet(
            customer = editing,
            onDismiss = { showForm = false },
            onSave = { name, email, phone, company, status ->
                val error = controller.saveCustomer(editing?.id, name, email, phone, company, status)
                if (error == null) {
                    showForm = false
                    scope.launch { snackbar.showSnackbar(if (editing == null) "Customer added" else "Customer updated") }
                }
                error
            }
        )
    }
    deleting?.let { customer ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            icon = { Icon(Icons.Default.Delete, null, tint = Coral) },
            title = { Text("Delete customer?") },
            text = { Text("${customer.name} will be permanently removed from this device.") },
            confirmButton = {
                Button(onClick = {
                    controller.deleteCustomer(customer); deleting = null
                    scope.launch { snackbar.showSnackbar("Customer deleted") }
                }, colors = ButtonDefaults.buttonColors(containerColor = Coral)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } }
        )
    }
    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false }, title = { Text("Log out?") },
            text = { Text("Your customer data will stay safely stored on this device.") },
            confirmButton = { Button(onClick = { confirmLogout = false; controller.logout() }) { Text("Log out") } },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun DashboardHeader(onLogout: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(Indigo), contentAlignment = Alignment.Center) {
            Text("C", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
        }
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text("Cliently", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Ink)
            Text("Customer workspace", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, "Log out", tint = Muted) }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp)) {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
            Text(label, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CustomerCard(customer: Customer, onEdit: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    var menuOpen by remember { mutableStateOf(false) }
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(avatarColor(customer.id)), contentAlignment = Alignment.Center) {
                    Text(initials(customer.name), color = Indigo, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(customer.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(customer.company, color = Muted, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
                StatusPill(customer.status)
                Box {
                    IconButton(onClick = { menuOpen = true }) { Icon(Icons.Default.MoreVert, "Actions", tint = Muted) }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text("Edit") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { menuOpen = false; onEdit() })
                        DropdownMenuItem(text = { Text("Delete", color = Coral) }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Coral) }, onClick = { menuOpen = false; onDelete() })
                    }
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 13.dp), color = Color(0xFFF0F1F6))
            DetailRow(Icons.Default.Email, customer.email)
            if (customer.phone.isNotBlank()) DetailRow(Icons.Default.Phone, customer.phone)
            Text("Added ${formatDate(customer.createdAt)}", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
        Icon(icon, null, tint = Muted, modifier = Modifier.size(16.dp))
        Text(value, color = Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun StatusPill(status: CustomerStatus) {
    val color = when (status) { CustomerStatus.ACTIVE -> Mint; CustomerStatus.LEAD -> Amber; CustomerStatus.INACTIVE -> Muted }
    Surface(color = color.copy(alpha = .12f), shape = RoundedCornerShape(50)) {
        Text(status.name.lowercase().replaceFirstChar { it.uppercase() }, color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
    }
}

@Composable
private fun EmptyCustomers(hasSearch: Boolean, onAdd: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(64.dp).clip(CircleShape).background(Indigo.copy(alpha = .1f)), contentAlignment = Alignment.Center) {
            Icon(if (hasSearch) Icons.Default.Search else Icons.Default.Group, null, tint = Indigo)
        }
        Text(if (hasSearch) "No matches found" else "No customers yet", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
        Text(if (hasSearch) "Try another search term" else "Add your first customer to get started", color = Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))
        if (!hasSearch) OutlinedButton(onClick = onAdd) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Add customer") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerFormSheet(
    customer: Customer?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, CustomerStatus) -> String?
) {
    var name by remember(customer) { mutableStateOf(customer?.name.orEmpty()) }
    var email by remember(customer) { mutableStateOf(customer?.email.orEmpty()) }
    var phone by remember(customer) { mutableStateOf(customer?.phone.orEmpty()) }
    var company by remember(customer) { mutableStateOf(customer?.company.orEmpty()) }
    var status by remember(customer) { mutableStateOf(customer?.status ?: CustomerStatus.LEAD) }
    var error by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    BackHandler(onBack = onDismiss)
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(start = 22.dp, end = 22.dp, bottom = 22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close") }
                Column(Modifier.padding(start = 4.dp)) {
                    Text(if (customer == null) "New customer" else "Edit customer", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text("Keep your customer details up to date", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(name, { name = it; error = null }, label = { Text("Full name *") }, leadingIcon = { Icon(Icons.Default.Person, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(email, { email = it; error = null }, label = { Text("Email *") }, leadingIcon = { Icon(Icons.Default.Email, null) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, leadingIcon = { Icon(Icons.Default.Phone, null) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(company, { company = it; error = null }, label = { Text("Company *") }, leadingIcon = { Icon(Icons.Default.Business, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
            Text("Status", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomerStatus.entries.forEach { option ->
                    val selected = status == option
                    OutlinedButton(
                        onClick = { status = option }, shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected) Indigo.copy(alpha = .1f) else Color.Transparent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Indigo else Color(0xFFDADDE8))
                    ) { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }, color = if (selected) Indigo else Muted, fontSize = 12.sp) }
                }
            }
            if (error != null) Text(error!!, color = Coral, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 12.dp))
            Row(Modifier.fillMaxWidth().padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(14.dp)) { Text("Cancel") }
                Button(onClick = {
                    focusManager.clearFocus(); error = onSave(name, email, phone, company, status)
                }, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(14.dp)) { Text(if (customer == null) "Create" else "Save", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

private fun initials(name: String): String = name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
private fun avatarColor(id: Long): Color = listOf(Color(0xFFE9EAFF), Color(0xFFE1F7EF), Color(0xFFFFF0DD), Color(0xFFFFE8EC))[(id % 4).toInt()]
private fun formatDate(timestamp: Long): String = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(timestamp))
