import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.locatecrew.data.model.User
import com.example.locatecrew.data.store.LoggedInUserManager
import com.example.locatecrew.viewmodel.UserViewModel
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val currentUser = runBlocking { userViewModel.getCurrentUser() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(color = Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder for rounded image
                Image(
                    painter = // Make image rounded
                    rememberAsyncImagePainter(ImageRequest.Builder(LocalContext.current).data(
                        data = "https://via.placeholder.com/150" // Placeholder URL for image
                    ).apply(block = fun ImageRequest.Builder.() {
                        transformations(CircleCropTransformation()) // Make image rounded
                    }).build()
                    ),
                    contentDescription = "User Image",
                    modifier = Modifier.size(150.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (currentUser != null) {
                    ProfileAttribute("Username", currentUser.username)
                    currentUser.phone?.let { ProfileAttribute("Phone", it) }
                    currentUser.email?.let { ProfileAttribute("Email", it) }
                } else {
                    Text("Loading user data...", style = MaterialTheme.typography.titleSmall)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    LoggedInUserManager.setLoggedInUsername("")
                    navController.navigate("login")
                },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun ProfileAttribute(attribute: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(attribute, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
