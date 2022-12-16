package com.mobilicos.smotrofon.ui
import java.util.function.BinaryOperator
import java.util.function.IntBinaryOperator


import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.databinding.FragmentMainBottomAppBarBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigInteger
import kotlin.properties.Delegates


open class Shape(edges_: Int = 0) {
    var edges = edges_
}

class Rectangle(): Shape(edges_ = 4) {

}

class Circle(): Shape(edges_ = 0) {

}


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: FragmentMainBottomAppBarBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private fun collectionsReplace(collection_:List<Shape>) {

    }

    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)

        val sh = Shape()
        val rec = Rectangle()
        val cir = Circle()
        var shapes: List<Circle> = listOf()
        collectionsReplace(shapes)

        lifecycleScope.launch {
            val numbers = 0..100
            val flow: Flow<Int> = numbers.asFlow()

            flow.take(10).filter{it % 2 == 0}
                .map {it * 10}
                .collectLatest{
                    println("FLOW MAP $it")
                }

            flow.filter{it % 2 == 1}
                .collect{
                    println("FLOW FILTER $it")
                }

            flow.collect{
                println(it)
            }

            fun fibonacci(): Flow<BigInteger> = flow {
                var x = BigInteger.ZERO
                var y = BigInteger.ONE
                while (true) {
                    emit(x)
                    x = y.also {
                        y += x
                    }
                }
            }

            fibonacci().take(100).collect {
                println("FLOW $it")
            }

            var a = 5
            var b = 11
            println("NOT FLOW A: $a B: $b")
            a = b.also {
                println("NOT FLOW A: $a B: $b")
                b=a
                println("NOT FLOW A: $a B: $b")
            }
            println("NOT FLOW A: $a B: $b")
        }







        binding = FragmentMainBottomAppBarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.bottom_app_bar_host) as NavHostFragment
        navController = navHostFragment.navController
        val navView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        // setupActionBarWithNavController(this, navController)
        navView.setupWithNavController(navController)

        // Setup the ActionBar with navController and 3 top level destinations
        appBarConfiguration = AppBarConfiguration(setOf(R.id.lessonsScreen,
            R.id.mediaScreen,
            R.id.coursesContentScreen,
            R.id.channelsScreen,
            R.id.profileScreen))

        setupActionBarWithNavController(navController, appBarConfiguration)

        // Add menu items without overriding methods in the Activity
//        addMenuProvider(this)
    }

//    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//        // Add menu items here
//        menuInflater.inflate(R.menu.main_menu, menu)
//    }
//
//    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//        when (menuItem.itemId) {
//            android.R.id.home -> {
//                navController.popBackStack()
//                return true
//            }
//        }
//
//        return true
//    }

    private fun showMessage(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_INDEFINITE).setAction("OK!") {
        }.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (isChangingConfigurations) {
            // this callback is executed as a result of a configuration change (e.g. orientation change)
        } else {
            // no configuration change happens (e.g. a click on a home button would end up here)
        }
    }
}