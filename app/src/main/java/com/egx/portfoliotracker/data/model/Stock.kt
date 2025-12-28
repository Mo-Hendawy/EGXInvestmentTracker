package com.egx.portfoliotracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a stock listed on the Egyptian Stock Exchange (EGX)
 */
@Entity(tableName = "stocks")
data class Stock(
    @PrimaryKey
    val symbol: String,
    val nameEn: String,
    val nameAr: String,
    val sector: String,
    val isActive: Boolean = true
)

/**
 * Pre-defined list of EGX stocks with real symbols and names
 */
object EGXStocks {
    val stocks = listOf(
        // Banking Sector
        Stock("COMI", "Commercial International Bank", "البنك التجاري الدولي", "Banking"),
        Stock("QNBE", "Qatar National Bank Alahli", "بنك قطر الوطني الأهلي", "Banking"),
        Stock("CIEB", "Credit Agricole Egypt", "بنك كريدي أجريكول مصر", "Banking"),
        Stock("FAISAL", "Faisal Islamic Bank", "بنك فيصل الإسلامي المصري", "Banking"),
        Stock("SAIB", "Societe Arabe Internationale De Banque", "الشركة العربية الدولية للبنوك", "Banking"),
        Stock("ADIB", "Abu Dhabi Islamic Bank Egypt", "بنك أبوظبي الإسلامي مصر", "Banking"),
        Stock("EGBE", "Egyptian Gulf Bank", "البنك المصري الخليجي", "Banking"),
        Stock("HDBK", "Housing & Development Bank", "بنك التعمير والإسكان", "Banking"),
        
        // Real Estate
        Stock("TMGH", "Talaat Moustafa Group", "مجموعة طلعت مصطفى", "Real Estate"),
        Stock("MNHD", "Madinet Nasr Housing", "مدينة نصر للإسكان والتعمير", "Real Estate"),
        Stock("PHDC", "Palm Hills Development", "بالم هيلز للتعمير", "Real Estate"),
        Stock("EMFD", "Emaar Misr Development", "إعمار مصر للتنمية", "Real Estate"),
        Stock("AMER", "Amer Group Holding", "عامر جروب", "Real Estate"),
        Stock("HELI", "Heliopolis Housing", "مصر الجديدة للإسكان والتعمير", "Real Estate"),
        Stock("SODIC", "Six of October Development", "السادس من أكتوبر للتنمية والاستثمار", "Real Estate"),
        Stock("ORHD", "Ora Developers Egypt", "أورا للتطوير العقاري", "Real Estate"),
        
        // Food & Beverage
        Stock("JUFO", "Juhayna Food Industries", "جهينة للصناعات الغذائية", "Food & Beverage"),
        Stock("EAST", "Eastern Company", "الشرقية للدخان", "Food & Beverage"),
        Stock("ISMA", "Ismailia Misr Poultry", "الإسماعيلية مصر للدواجن", "Food & Beverage"),
        Stock("EFID", "Edita Food Industries", "إديتا للصناعات الغذائية", "Food & Beverage"),
        Stock("DOMTY", "Arabian Food Industries (Domty)", "العربية للصناعات الغذائية (دومتي)", "Food & Beverage"),
        Stock("POUL", "Cairo Poultry", "القاهرة للدواجن", "Food & Beverage"),
        Stock("DSCW", "Delta Sugar", "الدلتا للسكر", "Food & Beverage"),
        Stock("SUGR", "Egyptian Sugar & Integrated", "السكر والصناعات التكاملية", "Food & Beverage"),
        Stock("MODI", "Modern Dairy", "الألبان الحديثة", "Food & Beverage"),
        
        // Building Materials & Construction
        Stock("SWDY", "El Sewedy Electric", "السويدي اليكتريك", "Building Materials"),
        Stock("ARCC", "Arabian Cement", "أسمنت العربية", "Building Materials"),
        Stock("SVCE", "Suez Cement", "السويس للأسمنت", "Building Materials"),
        Stock("PRCL", "Porcelain Cleopatra", "كليوباترا للسيراميك", "Building Materials"),
        Stock("EGCH", "Egyptian Chemical Industries", "الكيماويات المصرية", "Building Materials"),
        Stock("SPIN", "Sinai Cement", "سيناء للأسمنت", "Building Materials"),
        Stock("ELRS", "Ezz Steel", "حديد عز", "Building Materials"),
        Stock("ESRS", "Ezz Aldekhela Steel", "عز الدخيلة للصلب", "Building Materials"),
        Stock("ALUM", "Arab Aluminum Company", "العربية للألومنيوم", "Building Materials"),
        Stock("NCGC", "National Cement", "الاهلي للأسمنت", "Building Materials"),
        Stock("ATQA", "Atqa Cement", "أسمنت أطقا", "Building Materials"),
        Stock("MNCC", "Misr National Cement", "الوطنية للأسمنت", "Building Materials"),
        
        // Oil & Gas
        Stock("AMOC", "Alexandria Mineral Oils", "اسكندرية للزيوت المعدنية", "Oil & Gas"),
        Stock("SKPC", "Sidi Kerir Petrochemicals", "سيدي كرير للبتروكيماويات", "Oil & Gas"),
        
        // Financial Services
        Stock("HRHO", "EFG Holding", "إي أف جي القابضة", "Financial Services"),
        Stock("CIHE", "CI Capital Holding", "سي آي كابيتال", "Financial Services"),
        Stock("EKHOA", "Egyptian Kuwait Holding", "المصرية الكويتية للاستثمار", "Financial Services"),
        
        // Telecommunications
        Stock("ETEL", "Telecom Egypt", "المصرية للاتصالات", "Telecommunications"),
        Stock("ORWE", "Orange Egypt", "اورنج مصر", "Telecommunications"),
        Stock("EMOB", "E-Finance", "إي فاينانس", "Telecommunications"),
        
        // Healthcare & Pharmaceuticals
        Stock("MNPH", "Misr National Pharmaceutical", "القومية لإنتاج الأدوية", "Healthcare"),
        Stock("DCRC", "Delta for Construction and Rebuilding", "دلتا للإنشاء والتعمير", "Real Estate"),
        Stock("PHAR", "Egyptian International Pharmaceutical Industries Company", "الشركة المصرية الدولية للصناعات الدوائية", "Healthcare"),
        Stock("ADCI", "Alexandria Medical Services", "الاسكندرية للخدمات الطبية", "Healthcare"),
        Stock("CLHO", "Cleopatra Hospital Group", "مجموعة كليوباترا للمستشفيات", "Healthcare"),
        Stock("IBAG", "Ibnsina Pharma", "ابن سينا فارما", "Healthcare"),
        Stock("EPCO", "Egyptian Pharma", "المصرية للأدوية والكيماويات", "Healthcare"),
        Stock("SPMD", "Speed Medical", "سبيد ميديكال", "Healthcare"),
        
        // Industrial
        Stock("ASCM", "ASEC Company for Mining", "أسيك للتعدين", "Industrial"),
        Stock("MFPC", "Misr Fertilizers Production Company (MOPCO)", "مصر للأسمدة والصناعات الكيماوية (موبكو)", "Industrial"),
        Stock("EGAL", "Egypt Aluminum", "مصر للألومنيوم", "Industrial"),
        Stock("GBCO", "GB Corp", "جي بي كورب", "Industrial"),
        Stock("ELKA", "El Kabel", "مصنع الكابلات الكهربائية", "Industrial"),
        Stock("ECAP", "Egypt Caps", "ايجيكاب للأغطية", "Industrial"),
        Stock("UEGC", "El-Saeed Company for Contracting and Real Estate Investment", "شركة السعيد للمقاولات والاستثمار العقاري", "Industrial"),
        Stock("ENGC", "Engineering Industries", "الهندسية للصناعات", "Industrial"),
        Stock("ORAS", "Orascom Construction", "أوراسكوم كونستراكشن", "Industrial"),
        Stock("GPCP", "Giza General Contracting", "الجيزة العامة للمقاولات", "Industrial"),
        Stock("MPRC", "Middle East Packaging", "الشرق الأوسط للتعبئة", "Industrial"),
        Stock("RTVC", "El Nasr Transformers", "النصر للمحولات", "Industrial"),
        
        // Textiles & Apparel
        Stock("ELGD", "El Nasr Clothes & Textiles (Kabo)", "النصر للملابس والمنسوجات", "Textiles"),
        
        // Tourism & Hotels
        Stock("OCDI", "Orascom Development Egypt", "أوراسكوم للتنمية مصر", "Tourism"),
        Stock("MTIE", "Misr Tourism Investment", "مصر للاستثمارات السياحية", "Tourism"),
        
        // Transportation & Logistics
        Stock("ACGC", "Alex Containers & Goods", "اسكندرية للحاويات والبضائع", "Transportation"),
        Stock("ALCN", "Alexandria Containers", "اسكندرية للحاويات", "Transportation"),
        Stock("CCRS", "Cairo Transport", "القاهرة للنقل", "Transportation"),
        Stock("EGTS", "Egyptian Transport (EGYTRANS)", "مصر للنقل", "Transportation"),
        
        // Diversified Holdings
        Stock("ABUK", "Abu Qir Fertilizers and Chemical Industries Company", "أبو قير للأسمدة والصناعات الكيماوية", "Diversified"),
        Stock("BONY", "Bonyan for Development and Trade", "بنيان للتطوير والتجارة", "Diversified"),
        Stock("MICH", "Misr Chemical Industries", "مصر لصناعة الكيماويات", "Diversified"),
        Stock("OREG", "Oriental Weavers", "السجاد الشرقي", "Diversified"),
        Stock("EKHO", "Egyptian Kuwait Holding", "المصرية الكويتية", "Diversified"),
        
        // Services
        Stock("PRSC", "Premier Services", "بريميير للخدمات", "Services"),
        
        // Technology
        Stock("EGSA", "Egypt Sat", "ايجيسات", "Technology")
    )
    
    fun getStockBySymbol(symbol: String): Stock? = stocks.find { it.symbol == symbol }
    
    fun searchStocks(query: String): List<Stock> = stocks.filter {
        it.symbol.contains(query, ignoreCase = true) ||
        it.nameEn.contains(query, ignoreCase = true) ||
        it.nameAr.contains(query, ignoreCase = true)
    }
    
    fun getStocksBySector(sector: String): List<Stock> = stocks.filter {
        it.sector.equals(sector, ignoreCase = true)
    }
    
    fun getAllSectors(): List<String> = stocks.map { it.sector }.distinct().sorted()
}
