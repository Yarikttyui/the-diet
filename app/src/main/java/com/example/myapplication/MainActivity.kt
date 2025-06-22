package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Meal
import com.example.myapplication.data.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var dao: com.example.myapplication.data.AppDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getInstance(applicationContext)
        dao = db.appDao()

        lifecycleScope.launch {
            populateProductsIfNeeded()
        }

        setContent {
            MyAppTheme {
                NutritionScreen()
            }
        }
    }

    private suspend fun populateProductsIfNeeded() {
        withContext(Dispatchers.IO) {
            val existing = dao.getAllProducts()
            if (existing.isEmpty()) {
                val products = listOf(
                    Product(name = "Яблоко", caloriesPer100g = 52),
                    Product(name = "Банан", caloriesPer100g = 89),
                    Product(name = "Апельсин", caloriesPer100g = 47),
                    Product(name = "Курица", caloriesPer100g = 165),
                    Product(name = "Хлеб", caloriesPer100g = 250),
                    Product(name = "Молоко", caloriesPer100g = 42),
                    Product(name = "Сыр", caloriesPer100g = 402),
                    Product(name = "Яйцо", caloriesPer100g = 155),
                    Product(name = "Рис", caloriesPer100g = 130),
                    Product(name = "Картофель", caloriesPer100g = 77),
                    Product(name = "Морковь", caloriesPer100g = 41),
                    Product(name = "Огурец", caloriesPer100g = 16),
                    Product(name = "Помидор", caloriesPer100g = 18),
                    Product(name = "Свинина", caloriesPer100g = 242),
                    Product(name = "Говядина", caloriesPer100g = 250),
                    Product(name = "Индейка", caloriesPer100g = 135),
                    Product(name = "Лосось", caloriesPer100g = 208),
                    Product(name = "Творог", caloriesPer100g = 98),
                    Product(name = "Йогурт", caloriesPer100g = 59),
                    Product(name = "Гречка", caloriesPer100g = 92),
                    Product(name = "Капуста", caloriesPer100g = 25),
                    Product(name = "Брокколи", caloriesPer100g = 34),
                    Product(name = "Лук", caloriesPer100g = 40),
                    Product(name = "Чеснок", caloriesPer100g = 149),
                    Product(name = "Кефир", caloriesPer100g = 40),
                    Product(name = "Авокадо", caloriesPer100g = 160),
                    Product(name = "Мед", caloriesPer100g = 304),
                    Product(name = "Орехи", caloriesPer100g = 607),
                    Product(name = "Кукуруза", caloriesPer100g = 86),
                    Product(name = "Шоколад", caloriesPer100g = 546)
                )
                dao.insertProducts(products)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NutritionScreen() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var products by remember { mutableStateOf<List<Product>>(emptyList()) }
        var meals by remember { mutableStateOf<List<Meal>>(emptyList()) }
        var totalCalories by remember { mutableStateOf(0) }

        var productName by remember { mutableStateOf("") }
        var grams by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                products = dao.getAllProducts()
                meals = dao.getMealsByDate(getTodayStartMillis(), getTodayEndMillis())
            }
            totalCalories = meals.sumOf { it.calories }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    "Добавить прием пищи",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Название продукта") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        singleLine = true,
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = "Выбрать продукт"
                                )
                            }
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    ) {
                        products.forEach { product ->
                            DropdownMenuItem(
                                text = { Text(product.name) },
                                onClick = {
                                    productName = product.name
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = grams,
                    onValueChange = {
                        if (it.all { ch -> ch.isDigit() }) grams = it
                    },
                    label = { Text("Ваши Граммы") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val name = productName.trim()
                            val gramAmount = grams.toIntOrNull() ?: 0
                            if (name.isEmpty() || gramAmount <= 0) {
                                Toast.makeText(context, "Введите корректные данные", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            val product = withContext(Dispatchers.IO) { dao.getProductByName(name) }
                            if (product != null) {
                                val calories = product.caloriesPer100g * gramAmount / 100
                                val meal = Meal(
                                    productName = name,
                                    grams = gramAmount,
                                    calories = calories,
                                    timestamp = System.currentTimeMillis()
                                )
                                withContext(Dispatchers.IO) { dao.insertMeal(meal) }
                                meals = withContext(Dispatchers.IO) { dao.getMealsByDate(getTodayStartMillis(), getTodayEndMillis()) }
                                totalCalories = meals.sumOf { it.calories }
                                productName = ""
                                grams = ""
                            } else {
                                Toast.makeText(context, "Продукт не юыл найден", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Добавить продкут", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(30.dp))

                Text(
                    "Общие калории за сегодня: $totalCalories",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )

                Spacer(Modifier.height(16.dp))

                if (meals.isEmpty()) {
                    Text(
                        "Список приемов пищи пуст",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(meals, key = { it.timestamp }) { meal ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                MealCard(meal = meal)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MealCard(meal: Meal) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        meal.productName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${meal.grams} г",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${meal.calories} ккал",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    private fun getTodayStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getTodayEndMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}

@Composable
fun MyAppTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = Color(0xFF81D4FA),
        secondary = Color(0xFFFFCC80),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        onSurfaceVariant = Color(0xFFBBBBBB)
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
