package com.example.essentialcomponents

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.essentialcomponents.layout.ScaffoldExample
import com.example.essentialcomponents.ui.theme.EssentialComponentsTheme
import com.example.essentialcomponents.text.TextExample
import com.example.essentialcomponents.layout.BoxExample
import com.example.essentialcomponents.layout.ColumnExample
import com.example.essentialcomponents.layout.RowExample
import com.example.essentialcomponents.layout.SurfaceExample
import com.example.essentialcomponents.layout.CardExample
import com.example.essentialcomponents.layout.SpacerExample
import com.example.essentialcomponents.layout.HorizontalDividerExample
import com.example.essentialcomponents.list.ListWithDividerExample
import com.example.essentialcomponents.list.LazyColumnExample
import com.example.essentialcomponents.list.LazyRowExample
import com.example.essentialcomponents.list.LazyVerticalGridExample
import com.example.essentialcomponents.list.LazyHorizontalGridExample
import com.example.essentialcomponents.text.TextFieldExample
import com.example.essentialcomponents.text.OutlinedTextFieldExample
import com.example.essentialcomponents.text.BasicTextFieldExample
import com.example.essentialcomponents.button.ButtonExample
import com.example.essentialcomponents.button.TextButtonExample
import com.example.essentialcomponents.button.OutlinedButtonExample
import com.example.essentialcomponents.button.IconButtonExample
import com.example.essentialcomponents.button.FloatingActionButtonExample
import com.example.essentialcomponents.button.ExtendedFloatingActionButtonExample
import com.example.essentialcomponents.button.FilledTonalButtonExample
import com.example.essentialcomponents.selection.CheckboxExample
import com.example.essentialcomponents.selection.SwitchExample
import com.example.essentialcomponents.selection.RadioButtonExample
import com.example.essentialcomponents.selection.SliderExample
import com.example.essentialcomponents.selection.RangeSliderExample
import com.example.essentialcomponents.appbar.TopAppBarExample
import com.example.essentialcomponents.appbar.CenterAlignedTopAppBarExample
import com.example.essentialcomponents.appbar.MediumTopAppBarExample
import com.example.essentialcomponents.appbar.LargeTopAppBarExample
import com.example.essentialcomponents.appbar.BottomAppBarExample
import com.example.essentialcomponents.navigation.NavigationBarExample
import com.example.essentialcomponents.navigation.NavigationRailExample
import com.example.essentialcomponents.navigation.NavigationDrawerExample
import com.example.essentialcomponents.navigation.TabExample
import com.example.essentialcomponents.navigation.TabRowExample
import com.example.essentialcomponents.navigation.ScrollableTabRowExample
import com.example.essentialcomponents.dialogs.AlertDialogExample
import com.example.essentialcomponents.dialogs.CustomDialogExample
import com.example.essentialcomponents.dialogs.ModalBottomSheetExample
import com.example.essentialcomponents.dialogs.ModalDrawerSheetExample
import com.example.essentialcomponents.dialogs.DropdownMenuExample
import com.example.essentialcomponents.dialogs.DropdownMenuItemExample
import com.example.essentialcomponents.progress.CircularProgressIndicatorIndeterminateExample
import com.example.essentialcomponents.progress.CircularProgressIndicatorDeterminateExample
import com.example.essentialcomponents.progress.LinearProgressIndicatorIndeterminateExample
import com.example.essentialcomponents.progress.LinearProgressIndicatorDeterminateExample
import com.example.essentialcomponents.chips.AssistChipExample
import com.example.essentialcomponents.chips.FilterChipExample
import com.example.essentialcomponents.chips.InputChipExample
import com.example.essentialcomponents.chips.SuggestionChipExample
import com.example.essentialcomponents.badge.BadgeExample
import com.example.essentialcomponents.badge.BadgedBoxExample

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
                ListWithDividerExample()
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
            }
        }
    }
}