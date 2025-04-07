package com.example.cataniaunited.ui.tutorial

import android.R.attr.onClick
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import com.example.cataniaunited.R
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.ui.theme.catanGold
import com.example.cataniaunited.ui.theme.catanRessourceBar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp


@Composable
fun TutorialScreen(onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.catan_starting_page_background),
            contentDescription = "Tutorial Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(430.dp)
                .height(400.dp)
                .border(
                    width = 9.dp,
                    color = catanRessourceBar,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(
                    catanGold.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TutorialHeading("TUTORIAL")
                GeneralParagraph(
                    "In Catan, each player aims to become the most successful settler by reaching 10 Victory Points (VPs)." +
                            " You start with 2 points by placing 2 settlements, and you gain more VPs by building additional settlements, upgrading them to cities, or acquiring special cards / achievements. " +
                            "The first player to reach 10 VPs on their turn immediately wins the game."
                )

                GeneralHeading("BASIC CONCEPTS")
                FirstSubheading("1.\tSettlements & Cities")
                GeneralParagraph(
                    "o\tA Settlement is worth 1 VP. It collects 1 resource card from each adjacent terrain whenever that terrain’s number is rolled.\n" +
                            "\n" +
                            "o\tA City is worth 2 VPs. It collects 2 resource cards (double) from each adjacent terrain whose number is rolled. You create a city by upgrading one of your existing settlements.\n"
                )

                GeneralSubheading("2.\tRoads")
                GeneralParagraph(
                    "o\tRoads connect your settlements and cities. You can only build a new settlement on an intersection if it’s connected to your existing road network and it respects the “distance rule” (see below).\n" + "\n" +
                            "o\tThere’s a special award called Longest Road. Once you have a continuous path of 5 or more road pieces, you earn 2 extra VPs. Another player who later builds an even longer road takes the Longest Road card (and its VPs) from you.\n"
                )
                GeneralSubheading("3.\tDistance Rule")
                GeneralParagraph("o\tA settlement must be placed at least 2 intersections away from any other settlement or city. Essentially, you cannot build a new settlement if another settlement or city is directly next door.")
                GeneralSubheading("4.\tResource Cards")
                GeneralParagraph(
                    "o\tThere are 5 types of resources: Brick, Lumber, Wool, Grain, and Ore.\n" + "\n" +
                            "o\tYou use these to build roads, settlements, or cities, or to buy development cards (special cards that might give you knights, progress powers, or extra VP)."
                )
                GeneralSubheading("5.\tNumber Tokens & Dice")
                GeneralParagraph("o	Each terrain tile has a number (2 through 12, skipping 7). On a player’s turn, they roll 2 dice. The sum indicates which terrains produce resources. If you have a settlement or city bordering a terrain with that number, you gain the corresponding resources. (Settlements gain 1 resource; cities gain 2 resources.)")
                GeneralSubheading("6.\tThe Robber & Rolling a 7")
                GeneralParagraph(
                    "o\tIf a 7 is rolled, no terrain produces resources that turn. Instead, every player with more than 7 resource cards must discard half (rounded down).\n" +
                            "\n" +
                            "o\tThe player who rolled the 7 then moves the Robber onto any terrain tile (or the desert) and steals 1 random resource card from one player with a settlement/city next to that tile. Tiles under the Robber do not produce resources until the Robber is moved again."
                )
                GeneralSubheading("7.\tDevelopment Cards")
                GeneralParagraph(
                    "o\tYou can buy development cards (like Knight, Progress, or Victory Point) with resources (1 Wool, 1 Grain, 1 Ore).\n" +
                            "\n" +
                            "o\tKnights allow you to move the Robber (like rolling a 7). If you play 3 Knight cards, you earn the Largest Army award (+2 VPs), which can be taken by a player who later has more knights face-up than you.\n" +
                            "\n" +
                            "o\tProgress cards provide immediate special effects (e.g., build free roads, monopolize a resource, etc.).\n" +
                            "\n" +
                            "o\tVictory Point cards stay hidden until they help you reach 10 VPs, at which point you reveal them and win."
                )
                GeneralHeading("TURN SEQUENCE")
                GeneralParagraph("Each player’s turn has 3 main phases:")
                GeneralSubheading("1.\tRoll for Production")
                GeneralParagraph(
                    "o\tRoll 2 dice, sum them, and check the terrain tiles with that number.\n" +
                            "\n" +
                            "o\tEach settlement/city bordering that tile yields resource cards for its owner (unless blocked by the Robber)."
                )
                GeneralSubheading("2.\tTrade")
                GeneralParagraph(
                    "o\tDomestic Trade: Negotiate with other players, swapping resources. (Only the current player can trade with others, not the other players among themselves.)\n" +
                            "\n" +
                            "o\tMaritime Trade: Trade directly with the “bank” at a fixed rate of 4:1 (four matching resource cards for one of your choice).\n" +
                            "\n" +
                            "\n" +
                            "\uF0A7\tIf you have a settlement/city on a “harbor,” you can trade more favorably (e.g., 3:1 for any resource or even 2:1 for a specific resource, depending on the harbor)."
                )
                GeneralSubheading("3.\tBuild")
                GeneralParagraph(
                    "o\tSpend resource cards to build new roads or settlements, upgrade a settlement to a city, or buy a development card.\n" +
                            "o\tRoad = Brick + Lumber\n" +
                            "o\tSettlement = Brick + Lumber + Wool + Grain\n" +
                            "o\tCity (upgrade) = 2 Grain + 3 Ore\n" +
                            "o\tDevelopment Card = Wool + Grain + Ore\n" +
                            "o\tYou may build as many items as your resources allow."
                )
                GeneralHeading("WINNING THE GAME")
                GeneralParagraph(
                    "o\tYou start with 2 VPs from your initial 2 settlements.\n" +
                            "\n" +
                            "o\tYou can gain more VPs by:\n" +
                            "1.\tBuilding more settlements (each +1 VP).\n" +
                            "2.\tUpgrading to cities (each city +2 VPs, but it replaces a settlement).\n" +
                            "3.\tHolding Longest Road (2 VPs) or Largest Army (2 VPs).\n" +
                            "4.\tRevealing any Victory Point cards you’ve purchased.\n" +
                            "\n" +
                            "o\tThe moment you reach 10 VPs on your turn, you claim victory and the game ends immediately."
                )
                GeneralHeading("KEY TIPS & NOTES")
                GeneralParagraph(
                    "o\tHigh Numbers: Tiles labeled 6 or 8 (marked in red) are rolled most often. Settlements bordering these tiles often yield resources more frequently.\n" +
                            "\n" +
                            "o\tTrading: Active trading speeds up your progress. Offer deals that help both parties.\n" +
                            "\n" +
                            "\n" +
                            "o\tBlocking: Placing a road or settlement can block an opponent’s route for the Longest Road or hamper them from reaching a key spot.\n" +
                            "\n" +
                            "o\tRobber Strategy: Use the Robber to block high-producing tiles of your opponents, especially those near 6 or 8.\n" +
                            "\n" +
                            "o\tBalancing Resources: If you have too many of one resource, try to trade or place a settlement on a special harbor."
                )

            }

        }

        Button(
            onClick = onBackClick,
            border = BorderStroke(1.dp, Color.Black),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)

        ){
            Text(
                text = "BACK",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp)
            )
        }


    }
}

@Composable
fun TutorialHeading(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                catanRessourceBar,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(vertical = 8.dp)
    ) {

        Text(
            text = title,
            color = catanGold,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()

        )
    }
}

@Composable
fun GeneralHeading(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                catanRessourceBar,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(vertical = 8.dp)
    ) {

        Text(
            text = title,
            color = catanGold,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()

        )
    }
}

@Composable
fun FirstSubheading(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            color = Color.Black,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, top = 10.dp)
        )
    }
}

@Composable
fun GeneralSubheading(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            color = Color.Black,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, top = 0.dp, bottom = 0.dp)
        )
    }
}

@Composable
fun GeneralParagraph(text: String) {

    Text(
        text = text,
        color = Color.Black,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, top = 15.dp, end = 0.dp, bottom = 15.dp),
        textAlign = TextAlign.Justify

    )

}

@Preview(
    name = "Tutorial Screen - Landscape",
    widthDp = 891,
    heightDp = 411,
    showBackground = true
)

@Composable
fun TutorialScreenPreview() {
    CataniaUnitedTheme(darkTheme = false, dynamicColor = false) {
        TutorialScreen(onBackClick = {})
    }
}




