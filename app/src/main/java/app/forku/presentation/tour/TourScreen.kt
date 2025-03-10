package app.forku.presentation.tour

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.forku.R
import app.forku.presentation.navigation.Screen
import kotlinx.coroutines.launch

data class TourPage(
    val title: String,
    val description: String,
    val imageRes: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TourScreen(
    navController: NavController,
    viewModel: TourViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    
    val pages = listOf(
        TourPage(
            "Driver safety and team compliance on any device.",
            "Learn why you're here.",
            R.drawable.tour_1
        ),
        TourPage(
            "Track compliance and safety in any workplace.",
            "Reflections and goals",
            R.drawable.tour_2
        ),
        TourPage(
            "Track tasks with your team or while working solo.",
            "Goals and Reflection",
            R.drawable.tour_3
        ),
        TourPage(
            "Follow your goals and find your motivation.",
            "Register to begin",
            R.drawable.tour_4
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onEvent(TourEvent.NextPage)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            TourPage(pages[page])
        }

        // Page indicator
        Row(
            Modifier
                .height(50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                }
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .background(color, MaterialTheme.shapes.small)
                        .size(8.dp)
                )
            }
        }

        // Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { 
                    navController.navigate("register") {
                        popUpTo("tour") { inclusive = true }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Register")
            }
            
            Button(
                onClick = { 
                    navController.navigate("login") {
                        popUpTo("tour") { inclusive = true }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Login")
            }
        }
    }
}

@Composable
private fun TourPage(page: TourPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 32.dp)
        )
        
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}