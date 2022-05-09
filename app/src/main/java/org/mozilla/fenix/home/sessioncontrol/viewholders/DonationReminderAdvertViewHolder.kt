package org.mozilla.fenix.home.sessioncontrol.viewholders

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import mozilla.components.lib.state.ext.observeAsComposableState
import org.mozilla.fenix.R
import org.mozilla.fenix.compose.ComposeViewHolder
import org.mozilla.fenix.utils.loadPicture
import org.mozilla.fenix.components.components
import org.mozilla.fenix.components.feature.giveasyoulive.model.DonationReminderAdvert
import org.mozilla.fenix.home.sessioncontrol.DonationReminderAdvertInteractor
import org.mozilla.fenix.theme.FirefoxTheme

class DonationReminderAdvertViewHolder(
    composeView: ComposeView,
    viewLifecycleOwner: LifecycleOwner,
    private val interactor: DonationReminderAdvertInteractor
) : ComposeViewHolder(composeView, viewLifecycleOwner) {

    companion object {
        val LAYOUT_ID = View.generateViewId()
    }

    @Composable
    override fun Content() {

        val adverts = components.appStore
            .observeAsComposableState { state -> state.donationReminderAdverts }.value

        if (!adverts.isNullOrEmpty()) {

            Column {
                DonationReminderAdvertTheme(
                    adverts = adverts,
                    onAdvertClick = interactor::openAdvertPage
                )

            }

        }

    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun DonationReminderAdvertTheme(
    adverts: List<DonationReminderAdvert>  = emptyList(),
    onAdvertClick: (String) -> Unit) {


    val pagerState = rememberPagerState(adverts.size)

    LaunchedEffect(Unit) {
        while (true) {

            yield()
            delay(10000)

              pagerState.scrollToPage(
                page = (pagerState.currentPage + 1) % (pagerState.pageCount)
            )
        }
    }

    Column {

        HorizontalPager(
            count = adverts.size,
            state = pagerState,
            userScrollEnabled = false
        ) { page ->
            AdvertUpdate(
                adverts[page],
                onAdvertClick)
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
}

// Single card
@Composable
fun AdvertUpdate(ad: DonationReminderAdvert, onAdvertClick: (String) -> Unit) {

   val image =
        loadPicture(url = ad.imageUrl).value

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center

    ) {

        image?.let { img ->

            Image(
                bitmap = img.asImageBitmap(),
                modifier = Modifier
                    .clickable { onAdvertClick(ad.url) },
                contentScale = ContentScale.Fit,
                contentDescription = "Advert"

            )

        }
    }

}

