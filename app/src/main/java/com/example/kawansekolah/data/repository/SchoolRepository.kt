package com.example.kawansekolah.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import com.example.kawansekolah.data.model.GradeItem
import com.example.kawansekolah.data.model.Module
import com.example.kawansekolah.data.model.SchoolLevel
import com.example.kawansekolah.data.model.Subject
import com.example.kawansekolah.data.model.Task

object SchoolRepository {

    fun getInitialSubjects(): List<Subject> {
        return listOf(
            Subject(
                name = "Matematika",
                category = "Eksakta",
                modules = listOf(
                    Module(1, "Modul 1: Aljabar Dasar & Persamaan", "45 Menit", false),
                    Module(2, "Modul 2: Fungsi Kuadrat & Grafik", "60 Menit", false),
                    Module(3, "Modul 3: Matriks & Penjumlahan", "50 Menit", false),
                    Module(4, "Modul 4: Trigonometri Dasar", "55 Menit", false),
                    Module(5, "Modul 5: Geometri Analitik", "40 Menit", false),
                    Module(6, "Modul 6: Limit & Kontinuitas", "60 Menit", false),
                    Module(7, "Modul 7: Turunan Fungsi", "65 Menit", false),
                    Module(8, "Modul 8: Integral Tak Tentu", "70 Menit", false),
                    Module(9, "Modul 9: Vektor R2 & R3", "50 Menit", false),
                    Module(10, "Modul 10: Peluang & Kombinatorika", "45 Menit", false),
                    Module(11, "Modul 11: Statistika Deskriptif", "55 Menit", false),
                    Module(12, "Modul 12: Barisan & Deret", "60 Menit", false)
                ),
                icon = Icons.Default.Calculate,
                color = Color(0xFFE57373)
            ),
            Subject(
                name = "Bahasa Indonesia",
                category = "Bahasa",
                modules = listOf(
                    Module(13, "Modul 1: Teks Laporan Hasil Observasi", "40 Menit", false),
                    Module(14, "Modul 2: Teks Eksposisi & Gagasan", "45 Menit", false),
                    Module(15, "Modul 3: Teks Anekdot & Humor Kritikal", "35 Menit", false),
                    Module(16, "Modul 4: Hikayat & Cerita Rakyat", "50 Menit", false),
                    Module(17, "Modul 5: Puisi & Majas", "40 Menit", false),
                    Module(18, "Modul 6: Teks Negosiasi", "45 Menit", false),
                    Module(19, "Modul 7: Biografi Tokoh", "50 Menit", false),
                    Module(20, "Modul 8: Karya Tulis Ilmiah", "60 Menit", false)
                ),
                icon = Icons.Default.Book,
                color = Color(0xFF81C784)
            ),
            Subject(
                name = "Fisika",
                category = "Eksakta",
                modules = listOf(
                    Module(21, "Modul 1: Besaran & Satuan Vektor", "50 Menit", false),
                    Module(22, "Modul 2: Kinematika Gerak Lurus", "55 Menit", false),
                    Module(23, "Modul 3: Hukum Newton tentang Gerak", "60 Menit", false),
                    Module(24, "Modul 4: Usaha & Energi", "45 Menit", false),
                    Module(25, "Modul 5: Momentum & Impuls", "50 Menit", false),
                    Module(26, "Modul 6: Gerak Harmonis Sederhana", "55 Menit", false),
                    Module(27, "Modul 7: Dinamika Rotasi", "65 Menit", false),
                    Module(28, "Modul 8: Fluida Statis & Dinamis", "60 Menit", false),
                    Module(29, "Modul 9: Suhu & Kalor", "45 Menit", false),
                    Module(30, "Modul 10: Gelombang Bunyi & Cahaya", "50 Menit", false)
                ),
                icon = Icons.Default.Science,
                color = Color(0xFF64B5F6)
            ),
            Subject(
                name = "Biologi",
                category = "Eksakta",
                modules = listOf(
                    Module(31, "Modul 1: Keanekaragaman Hayati", "40 Menit", false),
                    Module(32, "Modul 2: Virus & Bakteri", "45 Menit", false),
                    Module(33, "Modul 3: Struktur Sel & Jaringan", "50 Menit", false),
                    Module(34, "Modul 4: Sistem Pencernaan", "55 Menit", false),
                    Module(35, "Modul 5: Sistem Pernapasan", "45 Menit", false),
                    Module(36, "Modul 6: Sistem Peredaran Darah", "60 Menit", false),
                    Module(37, "Modul 7: Fotosintesis & Respirasi", "50 Menit", false),
                    Module(38, "Modul 8: Genetik & Hereditas", "65 Menit", false)
                ),
                icon = Icons.Default.Biotech,
                color = Color(0xFFFFB74D)
            ),
            Subject(
                name = "Sejarah",
                category = "Sosial",
                modules = listOf(
                    Module(39, "Modul 1: Berpikir Sejarah & Diakronik", "35 Menit", false),
                    Module(40, "Modul 2: Manusia Purba di Indonesia", "40 Menit", false),
                    Module(41, "Modul 3: Kerajaan Hindu-Buddha", "50 Menit", false),
                    Module(42, "Modul 4: Kerajaan Islam Nusantara", "55 Menit", false),
                    Module(43, "Modul 5: Kolonialisme Bangsa Eropa", "60 Menit", false),
                    Module(44, "Modul 6: Pergerakan Nasional", "50 Menit", false)
                ),
                icon = Icons.Default.History,
                color = Color(0xFFBA68C8)
            )
        )
    }

    fun getInitialTasks(): List<Task> {
        return listOf(
            Task(1, "Latihan Soal Aljabar Bab 3", "Matematika", "Besok, 23:59", false),
            Task(2, "Menulis Essay Lingkungan Hidup", "Bahasa Indonesia", "20 Okt 2025", false),
            Task(3, "Praktikum Fotosintesis", "Ilmu Pengetahuan Alam", "22 Okt 2025", false),
            Task(4, "Quiz Trigonometri & Vektor", "Matematika", "25 Okt 2025", false),
            Task(5, "Laporan Praktikum Hukum Newton", "Fisika", "28 Okt 2025", false)
        )
    }

    val teacherNuptkList = listOf(
        "9452758659200032", "1845763665300021", "5041761662200013", "3756749652200023", "8234751653300012",
        "6149760661200033", "2938747650100012", "7450755657300021", "4635748651200042", "9158762664300011",
        "3847759660200022", "5732746649100031", "8421764666300013", "2056753655200021", "6943750652300042",
        "1359765667100012", "7824748651200033", "4538761663300021", "9217757658200012", "3645752654300031",
        "5139749651100022", "8752763665200041", "2436758660300013", "6051747649200021", "1948762664100032",
        "7325754656300011", "4819759661200023", "9534746648300042", "3247764667200012", "5821753655100031",
        "8156760662300022", "2739748650200013", "6453757659300041", "1048751653100021", "7632765666200032",
        "4927756658100011", "9351749652300023", "3146763664200042", "5628758660100012", "8935747651300031",
        "2249761663200022", "6754752654100013", "1538766668300041", "7123755657200021", "4457748650300032",
        "9812762665100011", "3546759661300023", "5239746649200042", "8651764667100012", "2348753656200031"
    )

    val studentNisnList = listOf(
        "8933022425", "4812922059", "4406044393", "7388684281", "7963562696", "9105994931", "4404583747", "6341051199", "2021342407", "1891275543",
        "7062378746", "5721188595", "3632935459", "8205287127", "4795085338", "6734821097", "3990071686", "9168488203", "5898101946", "4704253171",
        "1245884971", "3074821869", "9031403453", "4782147046", "2912242934", "5037913688", "4377552885", "9864378799", "4458737135", "5697231738",
        "9231155306", "2487201716", "7988390252", "2225269789", "8506171451", "1954855242", "6792377211", "8233625932", "2175827057", "6863022978",
        "1545278037", "4968652825", "2897423811", "7174592786", "2038349132", "9413536892", "1771671192", "5743245799", "9699953394", "3302252639",
        "9583210836", "8914803290", "5101696309", "8881006751", "2089908668", "3919055047", "5421285215", "1911058174", "3091662199", "5201409328",
        "4720824789", "9204361871", "5581027539", "4865015030", "4372189037", "4061655376", "7160618152", "7993373227", "1452818073", "2007851095",
        "6358573042", "4758067330", "1046432284", "7201376479", "5196468730", "4020897784", "1551880274", "6903815929", "4636800176", "1072471661",
        "5689706472", "6959976170", "3455913389", "2773113087", "6637197859", "7353764600", "4848002147", "7855748021", "4683645843", "8265401878",
        "3683157771", "5213250811", "3664816087", "9352910012", "4315368014", "5895158519", "2467401484", "6219504862", "3145717397", "2133444001",
        "1873062360", "6529620559", "5882835936", "7004291861", "3080910128", "2980452782", "2507631040", "3540568778", "7974567108", "7058724518",
        "6944427972", "8134975474", "8809084998", "3694695403", "5877649947", "9163780481", "9273152462", "7874376405", "9742105472", "5841083016",
        "2542783445", "7500130152", "7078762568", "6948467256", "7123001105", "8702209496", "3606639748", "8740562126", "8638298105", "7323426114",
        "9910646459", "4002037131", "9326652917", "1578447610", "6761529662", "2318115549", "9236545970", "4534638078", "9615144583", "2294196067",
        "2044037919", "9622772890", "3256604255", "2345907546", "9882936433", "1104537088", "4578112295", "6111859904", "9196393139", "7488709592",
        "6676740341", "3982024268", "8627703815", "5223677671", "7528558079", "2496891402", "4900855337", "4409766997", "3919955405", "5951258761",
        "7722119338", "1048170921", "6877275625", "8183107727", "8681994476", "4444337198", "7768888031", "8231982748", "4058583744", "6348032330",
        "5722576920", "2494122547", "9341551010", "4798910364", "6263885135", "4431566101", "5535033823", "1904518097", "7477204754", "5708786843",
        "9276638822", "4012347377", "6024340556", "8396827613", "6804142115", "3200542527", "2226588395", "2322242095", "4368441832", "7156547018",
        "9905619324", "6391943938", "4898499207", "3232375018", "5965063782", "1844753868", "1608571906", "3280718586", "2183913130", "6607545319",
        "9231478236", "4932931061", "1560603352", "6709826644", "1310629629", "4137535373", "7250468002", "1644051533", "6100647217", "9848115575",
        "7716556680", "8795353359", "6465004207", "7889549787", "4998328990", "5921436852", "6906734885", "7429237731", "5867017654", "8033708628",
        "6206341593", "8164467272", "6074366144", "2160172970", "1246146405", "2734122614", "8905470378", "9046646455", "8073476995", "9045319734",
        "9254633215", "2154509816", "3377623604", "2175179704", "7130312661", "2128622530", "9052727399", "1727152160", "1791840283", "4953653046",
        "2155410052", "9783635558", "2472250118", "9785271950", "2192752775", "3257528568", "8321733841", "7864842435", "5542193312", "8841280822",
        "4080507877", "6281587812", "3279046283", "6643075417", "7603758953", "1826348039", "5122604549", "1884955404", "3564544702", "4814831232",
        "4064704925", "5089801958", "1779611554", "3134618569", "9886032797", "3119746124", "9628107765", "2968286856", "8546004663", "3466273483",
        "4682020713", "9598650850", "4019414213", "2010120935", "4752722848", "5179499216", "6475499443", "5663140240", "8957334328", "4005100185",
        "7795972266", "1603400196", "1363150934", "7893287359", "4607955819", "1294080742", "5491373282", "4579657206", "8359490740", "4083269756",
        "1310204092", "7923079233", "4845174974", "2984382600", "9162424934", "8876369469", "7465060447", "3239821959", "1541340904", "8892309863"
    )

    fun isValidTeacherNuptkOrPassword(input: String): Boolean {
        val clean = input.trim()
        if (clean == "12345" || clean == "54321") return true
        return teacherNuptkList.contains(clean) || clean.length == 16
    }

    fun isValidStudentNisn(input: String): Boolean {
        val clean = input.trim()
        if (clean == "0052025100" || clean == "20251004") return true
        return studentNisnList.contains(clean) || (clean.length == 10 && clean.all { it.isDigit() })
    }

    fun getInitialGrades(): List<GradeItem> {
        val emptyNote = "Belum ada catatan dari Guru."
        val emptyStatus = "Belum Ada Catatan"
        return listOf(
            // === TINGKAT SMP - SEMESTER 1 (11 Mapel, Awal Nilai = 0, Catatan Kosong) ===
            GradeItem(1, "PENDIDIKAN AGAMA DAN BUDI PEKERTI", "AGM01", "Wajib", 2, 0.0, 1, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(2, "PENDIDIKAN PANCASILA", "PNC02", "Wajib", 2, 0.0, 1, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(3, "BAHASA INDONESIA", "IND03", "Wajib", 3, 0.0, 1, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(4, "MATEMATIKA", "MTK04", "Wajib", 4, 0.0, 1, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(5, "ILMU PENGETAHUAN ALAM (IPA)", "IPA05", "Wajib", 4, 0.0, 1, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(6, "ILMU PENGETAHUAN SOSIAL (IPS)", "IPS06", "Wajib", 4, 0.0, 1, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(7, "BAHASA INGGRIS", "ING07", "Wajib", 3, 0.0, 1, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(8, "PENDIDIKAN JASMANI, OLAHRAGA, DAN KESEHATAN (PJOK)", "PJK08", "Wajib", 2, 0.0, 1, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(9, "INFORMATIKA", "INF09", "Wajib", 3, 0.0, 1, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(10, "SENI DAN PRAKARYA", "SEN10", "Wajib", 2, 0.0, 1, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(11, "MUATAN LOKAL", "MLK11", "Wajib", 2, 0.0, 1, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),

            // === TINGKAT SMP - SEMESTER 2 (11 Mapel, Awal Nilai = 0, Catatan Kosong) ===
            GradeItem(12, "PENDIDIKAN AGAMA DAN BUDI PEKERTI", "AGM12", "Wajib", 2, 0.0, 2, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(13, "PENDIDIKAN PANCASILA", "PNC13", "Wajib", 2, 0.0, 2, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(14, "BAHASA INDONESIA", "IND14", "Wajib", 3, 0.0, 2, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(15, "MATEMATIKA", "MTK15", "Wajib", 4, 0.0, 2, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(16, "ILMU PENGETAHUAN ALAM (IPA)", "IPA16", "Wajib", 4, 0.0, 2, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(17, "ILMU PENGETAHUAN SOSIAL (IPS)", "IPS17", "Wajib", 4, 0.0, 2, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(18, "BAHASA INGGRIS", "ING18", "Wajib", 3, 0.0, 2, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(19, "PENDIDIKAN JASMANI, OLAHRAGA, DAN KESEHATAN (PJOK)", "PJK19", "Wajib", 2, 0.0, 2, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(20, "INFORMATIKA", "INF20", "Wajib", 3, 0.0, 2, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(21, "SENI DAN PRAKARYA", "SEN21", "Wajib", 2, 0.0, 2, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0),
            GradeItem(22, "MUATAN LOKAL", "MLK22", "Wajib", 2, 0.0, 2, SchoolLevel.SMP, emptyStatus, emptyNote, 0, 0, 0, 0)
        )
    }

    fun getAiResponse(query: String): String {
        val trimmed = query.trim()
        return when {
            trimmed.contains("aljabar", ignoreCase = true) || trimmed.contains("matematika", ignoreCase = true) ->
                "Untuk materi Aljabar, prinsip utamanya adalah menyederhanakan variabel. Contoh: 2x + 5 = 15, maka 2x = 10, sehingga x = 5. Apakah kamu ingin mencoba soal latihan Aljabar?"
            trimmed.contains("fisika", ignoreCase = true) || trimmed.contains("newton", ignoreCase = true) ->
                "Hukum Newton II menyatakan F = m × a (Gaya = massa × percepatan). Jika massa benda 2 kg dan percepatan 3 m/s², maka Gayanya adalah 6 Newton!"
            trimmed.contains("soal", ignoreCase = true) || trimmed.contains("latihan", ignoreCase = true) ->
                "Ini soal latihan untukmu:\n1. Hitung nilai x dari persamaan 3x - 9 = 0.\n2. Sebutkan 3 contoh zat padat, cair, dan gas!\nCoba jawab di sini ya!"
            trimmed.contains("rangkum", ignoreCase = true) || trimmed.contains("ringkas", ignoreCase = true) ->
                "Rangkuman singkat: Fokus belajar minggu ini mencakup persamaan dasar Aljabar, Hukum Newton, dan keanekaragaman hayati Biologi. Pastikan menyelesaikan modul sebelum kuis!"
            else ->
                "Pertanyaan yang bagus! Berdasarkan materi di Scolarsia, kamu bisa memperdalam pemahaman dengan membaca modul interaktif atau mencoba latihan soal di tab Pelajaran."
        }
    }
}
