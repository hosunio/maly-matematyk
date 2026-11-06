package pl.malymatematyk.app

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Forest = Color(0xFF176B52)
private val Ink = Color(0xFF18352B)
private val Canvas = Color(0xFFF5F8F6)
private val Mint = Color(0xFFE8F5EF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("maly_matematyk", Context.MODE_PRIVATE)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Forest, background = Canvas, surface = Color.White)) {
                MathApp(
                    initialGrade = prefs.getInt("grade", 3),
                    initialPoints = prefs.getInt("points", 0),
                    onSave = { grade, points -> prefs.edit().putInt("grade", grade).putInt("points", points).apply() }
                )
            }
        }
    }
}

internal enum class Track(val title: String, val icon: String, val subtitle: String) {
    ALL("Wszystkie", "✨", "Mieszany trening"), KANGAROO("Kangur", "🐾", "Matematyka konkursowa"),
    LEON("LEON", "🏆", "Trening konkursowy"), ALE_MOZGI("AleMózgi", "🧠", "Logika i algorytmika"), PYTHON("Python", "🐍", "Programowanie i algorytmika"),
    LOGIC("Logika", "🔎", "Łamigłówki"), MATH("Matematyka", "➗", "Rachunki i geometria")
}

internal data class Question(
    val grade: Int, val track: Track, val prompt: String, val options: List<String>,
    val answer: Int, val hint: String, val explanation: String
)

@Composable
private fun MathApp(initialGrade: Int, initialPoints: Int, onSave: (Int, Int) -> Unit) {
    val context = LocalContext.current
    var remoteTasks by remember { mutableStateOf(SupabaseTasks.loadCached(context)) }
    var syncLabel by remember { mutableStateOf(if (remoteTasks.isEmpty()) "Tryb offline · zadania w aplikacji" else "Zadania zapisane offline") }
    LaunchedEffect(Unit) {
        try {
            val fresh = withContext(Dispatchers.IO) { SupabaseTasks.fetchAndCache(context) }
            remoteTasks = fresh
            syncLabel = "Zsynchronizowano zadania online"
        } catch (_: Exception) {
            syncLabel = if (remoteTasks.isEmpty()) "Brak internetu · tryb offline" else "Offline · używam zapisanych zadań"
        }
    }
    var grade by remember { mutableIntStateOf(initialGrade.coerceIn(1, 8)) }
    var points by remember { mutableIntStateOf(initialPoints) }
    var track by remember { mutableStateOf(Track.ALL) }
    var index by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var checked by remember { mutableStateOf(false) }
    var hintVisible by remember { mutableStateOf(false) }
    val tasks = remember(grade, track, remoteTasks) { (questionBank + remoteTasks).filter { it.grade == grade && (track == Track.ALL || it.track == track) } }
    val task = tasks.getOrNull(index)
    fun reset() { selected = -1; checked = false; hintVisible = false }

    Scaffold(containerColor = Canvas, topBar = {
        Column(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 13.dp)) {
            Text("MAŁY MATEMATYK", color = Forest, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text("Akademia matematyki i logicznego myślenia", color = Color.Gray, fontSize = 12.sp)
        }
    }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp), contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp)
        ) {
            item {
                Column(Modifier.fillMaxWidth().background(Forest, RoundedCornerShape(23.dp)).padding(19.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Cześć, młody odkrywco!", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            Text("Ćwicz spryt, matematykę i logiczne myślenie.", color = Color.White.copy(alpha = .88f), fontSize = 12.sp)
                        }
                        Text("🧠", fontSize = 38.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("WYBIERZ KLASĘ", color = Color.White.copy(alpha = .85f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        (1..8).forEach { n ->
                            Box(Modifier.weight(1f).background(if (grade == n) Color.White else Color.White.copy(alpha = .17f), RoundedCornerShape(9.dp))
                                .clickable { grade = n; index = 0; reset(); onSave(grade, points) }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text("$n", color = if (grade == n) Forest else Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Zdobyte punkty", color = Color.White.copy(alpha = .8f), fontSize = 11.sp)
                            Text("$points", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Surface(color = Color.White.copy(alpha = .16f), shape = RoundedCornerShape(12.dp)) {
                            Text("✓ $syncLabel", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
                        }
                    }
                }
            }
            item { Text("WYBIERZ TRENING", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(Track.KANGAROO, Track.LEON, Track.ALE_MOZGI, Track.PYTHON, Track.LOGIC, Track.MATH).chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { t -> TrackCard(t, track == t, Modifier.weight(1f)) { track = if (track == t) Track.ALL else t; index = 0; reset() } }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                    TextButton(onClick = { track = Track.ALL; index = 0; reset() }) { Text("Pokaż wszystkie treningi") }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(if (track == Track.ALL) "Trening · klasa $grade" else "${track.title} · klasa $grade", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Wybierz jedną odpowiedź.", color = Color.Gray, fontSize = 12.sp)
                    }
                    if (tasks.isNotEmpty()) Text("${index + 1}/${tasks.size}", color = Forest, fontWeight = FontWeight.Bold)
                }
            }
            if (task == null) {
                item { InfoCard("Nowe zadania w przygotowaniu", "W tym dziale nie ma jeszcze zadań dla klasy $grade. Wybierz inny trening.") }
            } else {
                item {
                    Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(21.dp)).padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Mint, shape = RoundedCornerShape(8.dp)) { Text(task.track.title, color = Forest, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)) }
                            Spacer(Modifier.weight(1f)); Text("ZADANIE ${index + 1}", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(13.dp))
                        Text(task.prompt, color = Ink, fontSize = 18.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        task.options.forEachIndexed { i, option ->
                            val isCorrect = i == task.answer
                            val bg = when { checked && isCorrect -> Mint; checked && selected == i -> Color(0xFFFFEFED); selected == i -> Mint; else -> Color.White }
                            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp).background(bg, RoundedCornerShape(12.dp))
                                .clickable(enabled = !checked) { selected = i }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = if (selected == i || (checked && isCorrect)) Forest else Color(0xFFF0F4F1), shape = RoundedCornerShape(8.dp)) {
                                    Text(('A' + i).toString(), color = if (selected == i || (checked && isCorrect)) Color.White else Ink, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                }
                                Text(option, color = Ink, modifier = Modifier.weight(1f).padding(start = 12.dp), fontSize = 14.sp)
                                if (checked && isCorrect) Text("✓", color = Forest, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (!checked) {
                            TextButton(onClick = { hintVisible = !hintVisible }) { Text(if (hintVisible) "Ukryj podpowiedź" else "Pokaż podpowiedź") }
                            if (hintVisible) Text(task.hint, color = Forest, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().background(Mint, RoundedCornerShape(11.dp)).padding(12.dp))
                            Button(onClick = { if (selected >= 0) { checked = true; if (selected == task.answer) { points += 10; onSave(grade, points) } } }, enabled = selected >= 0, modifier = Modifier.fillMaxWidth()) { Text("Sprawdź odpowiedź") }
                        } else {
                            val right = selected == task.answer
                            Text(if (right) "Świetnie! +10 punktów 🎉" else "Tym razem się nie udało. Zobacz rozwiązanie.", color = if (right) Forest else Color(0xFFB0443D), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                            Text(task.explanation, color = Ink, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 7.dp))
                            Button(onClick = { index = (index + 1) % tasks.size; reset() }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text(if (index + 1 == tasks.size) "Wróć do pierwszego zadania" else "Następne zadanie →") }
                        }
                    }
                }
            }
            item { Text("Mały matematyk · synchronizacja Supabase + tryb offline", color = Color.Gray, fontSize = 11.sp) }
        }
    }
}

@Composable
private fun TrackCard(track: Track, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier.clickable(onClick = onClick), color = if (active) Mint else Color.White,
        shape = RoundedCornerShape(15.dp), border = BorderStroke(1.dp, if (active) Forest else Color(0xFFE5ECE7))) {
        Column(Modifier.padding(12.dp)) {
            Text(track.icon, fontSize = 22.sp); Spacer(Modifier.height(4.dp))
            Text(track.title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(track.subtitle, color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp)).padding(18.dp)) {
        Text(title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(body, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 7.dp), lineHeight = 19.sp)
    }
}

private val questionBank = listOf(
    Question(1, Track.KANGAROO, "Na ławce leżało 6 kasztanów. Zosia dołożyła jeszcze 4. Ile kasztanów leży teraz na ławce?", listOf("9", "10", "11", "12"), 1, "Do sześciu dodaj cztery.", "6 + 4 = 10. Na ławce leży 10 kasztanów."),
    Question(1, Track.LOGIC, "W szeregu są koła, kwadraty, koła, kwadraty, koła… Jaki kształt powinien być następny?", listOf("Koło", "Kwadrat", "Trójkąt", "Prostokąt"), 1, "Kształty występują na przemian.", "Po kole następuje kwadrat, ponieważ wzór powtarza się na przemian."),
    Question(2, Track.KANGAROO, "W autobusie było 12 osób. Wysiadły 4 osoby, a wsiadły 3. Ile osób jest teraz w autobusie?", listOf("9", "10", "11", "19"), 2, "Najpierw odejmij osoby, które wysiadły, potem dodaj wsiadające.", "12 − 4 + 3 = 11 osób."),
    Question(2, Track.LOGIC, "Uzupełnij ciąg: 2, 5, 8, 11, …", listOf("12", "13", "14", "15"), 2, "Sprawdź, o ile zwiększa się każda kolejna liczba.", "Za każdym razem dodajemy 3, więc 11 + 3 = 14."),
    Question(3, Track.KANGAROO, "W sali ustawiono 4 rzędy po 6 krzeseł. Ile krzeseł ustawiono razem?", listOf("20", "22", "24", "26"), 2, "Policz 4 grupy po 6 krzeseł.", "4 × 6 = 24 krzesła."),
    Question(3, Track.ALE_MOZGI, "Robot powtarza sekwencję: obrót w prawo, krok do przodu. Ile obrotów wykona po 5 powtórzeniach?", listOf("2", "4", "5", "10"), 2, "W każdej sekwencji jest dokładnie jeden obrót.", "Jedno powtórzenie to jeden obrót. 5 powtórzeń oznacza 5 obrotów."),
    Question(4, Track.KANGAROO, "Prostokąt ma boki długości 8 cm i 5 cm. Jaki jest jego obwód?", listOf("13 cm", "26 cm", "40 cm", "80 cm"), 1, "Dodaj długości wszystkich czterech boków.", "8 + 5 + 8 + 5 = 26 cm."),
    Question(4, Track.LEON, "Na 6 półkach ustawiono po 18 książek. Ile książek ustawiono razem?", listOf("98", "108", "118", "128"), 1, "Pomnóż liczbę półek przez liczbę książek na półce.", "6 × 18 = 108 książek."),
    Question(5, Track.KANGAROO, "Ola przeczytała 3/8 książki liczącej 160 stron. Ile stron przeczytała?", listOf("40", "50", "60", "80"), 2, "Najpierw oblicz 1/8 ze 160, a potem pomnóż przez 3.", "160 ÷ 8 = 20, a 20 × 3 = 60 stron."),
    Question(5, Track.ALE_MOZGI, "Algorytm zaczyna od liczby 2. W każdym kroku mnoży ją przez 2 i odejmuje 1. Jaki wynik otrzyma po dwóch krokach?", listOf("3", "5", "6", "7"), 1, "Wykonaj dwa razy: pomnóż przez 2, a następnie odejmij 1.", "Krok 1: 2 × 2 − 1 = 3. Krok 2: 3 × 2 − 1 = 5."),
    Question(6, Track.KANGAROO, "W klasie jest 28 uczniów. 3/7 uczniów to dziewczęta. Ilu jest chłopców?", listOf("12", "14", "16", "18"), 2, "Oblicz liczbę dziewcząt, a następnie odejmij ją od 28.", "3/7 z 28 to 12 dziewcząt. 28 − 12 = 16 chłopców."),
    Question(6, Track.ALE_MOZGI, "Zmienna x ma wartość 4. Program wykonuje x = x + 3, a następnie x = 2 × x. Jaka jest końcowa wartość x?", listOf("11", "12", "14", "16"), 2, "Najpierw dodaj 3, dopiero potem pomnóż wynik przez 2.", "Po pierwszej instrukcji x = 7. Po drugiej x = 14."),
    Question(7, Track.KANGAROO, "Cena plecaka po obniżce o 20% wynosi 120 zł. Ile kosztował przed obniżką?", listOf("140 zł", "144 zł", "150 zł", "160 zł"), 2, "Po obniżce została cena stanowiąca 80% ceny początkowej.", "120 zł to 80% ceny początkowej. 120 ÷ 0,8 = 150 zł."),
    Question(7, Track.ALE_MOZGI, "Zmienna n zaczyna od 1. Dopóki n jest mniejsze od 20, zwiększ n o 3. Jaka wartość zakończy działanie pętli?", listOf("19", "20", "21", "22"), 3, "Wypisz kolejne wartości po dodaniu 3 i zatrzymaj się, gdy n nie będzie już mniejsze od 20.", "Kolejne wartości to 1, 4, 7, 10, 13, 16, 19, 22. Przy 22 warunek n < 20 jest fałszywy."),
    Question(8, Track.KANGAROO, "W trójkącie prostokątnym przyprostokątne mają długości 6 cm i 8 cm. Ile wynosi przeciwprostokątna?", listOf("9 cm", "10 cm", "12 cm", "14 cm"), 1, "Zastosuj twierdzenie Pitagorasa.", "c² = 6² + 8² = 36 + 64 = 100, więc c = 10 cm."),
    Question(8, Track.ALE_MOZGI, "Jaka jest reszta z dzielenia liczby 23 przez 5?", listOf("2", "3", "4", "5"), 1, "Znajdź największą wielokrotność 5, która nie przekracza 23.", "23 = 4 × 5 + 3, więc reszta wynosi 3."),
    Question(8, Track.LEON, "Dwie liczby są w stosunku 3:5, a ich suma wynosi 64. Jaka jest większa liczba?", listOf("24", "32", "40", "48"), 2, "Suma części stosunku to 3 + 5. Podziel 64 przez tę sumę.", "Jedna część to 64 ÷ 8 = 8. Większa liczba to 5 × 8 = 40.")
)
