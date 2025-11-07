package com.example.essentialcomponents

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.essentialcomponents.ui.theme.EssentialComponentsTheme
import com.example.essentialcomponents.list.ListWithDividerExample
import com.example.essentialcomponents.icon.IconExample
import com.example.essentialcomponents.icon.PainterIconExample
import com.example.essentialcomponents.image.ImageExample
import com.example.essentialcomponents.image.BitmapImageExample
import com.example.essentialcomponents.image.AsyncImageExample
import com.example.essentialcomponents.image.AsyncImageWithPlaceholderExample

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EssentialComponentsTheme{

                //<< Layout Components >>
                //ScaffoldExample()
                //BoxExample()
                //ColumnExample()
                //RowExample()
                //SurfaceExample()
                //CardExample()
                //SpacerExample()
                //HorizontalDividerExample()

                //<< List Components >>
                //ListWithDividerExample()
                //LazyColumnExample()
                //LazyRowExample()
                //LazyVerticalGridExample()
                //LazyHorizontalGridExample()

                //<< Text & Input Components >>
                //TextExample()
                //TextFieldExample()
                //OutlinedTextFieldExample()
                //BasicTextFieldExample()

                //<< Buttons Components >>
                //ButtonExample()
                //TextButtonExample()
                //OutlinedButtonExample()
                //IconButtonExample()
                //FloatingActionButtonExample()
                //ExtendedFloatingActionButtonExample()
                //FilledTonalButtonExample()

                //<< Selection Components >>
                //CheckboxExample()
                //SwitchExample()
                //RadioButtonExample()
                //SliderExample()
                //RangeSliderExample()

                //<< App Bar Components >>
                //TopAppBarExample()
                //CenterAlignedTopAppBarExample()
                //MediumTopAppBarExample()
                //LargeTopAppBarExample()
                //BottomAppBarExample()

                //<< Navigation Components >>
                //NavigationBarExample()
                //NavigationRailExample()
                //NavigationDrawerExample()
                //TabExample()
                //TabRowExample()
                //ScrollableTabRowExample()

                //<< Dialogs Components >>
                //AlertDialogExample()
                //CustomDialogExample()
                //ModalBottomSheetExample()
                //ModalDrawerSheetExample()
                //DropdownMenuExample()
                //DropdownMenuItemExample()

                //<< Progress Components >>
                //CircularProgressIndicatorIndeterminateExample()
                //CircularProgressIndicatorDeterminateExample()
                //LinearProgressIndicatorIndeterminateExample()
                //LinearProgressIndicatorDeterminateExample()

                // << Chips Components >>
                //AssistChipExample()
                //FilterChipExample()
                //InputChipExample()
                //SuggestionChipExample()

                //<< Badge Components >>
                //BadgeExample()
                //BadgedBoxExample()

                //<< Icon Components >>
                //IconExample()
                //PainterIconExample()

                //<< Image Components >>
                //ImageExample()
                //BitmapImageExample()
                //AsyncImageExample()
                AsyncImageWithPlaceholderExample()
            }
        }
    }
}