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
        Stock("QNBA", "Qatar National Bank Alahly", "بنك قطر الوطني الأهلي", "Banking"),
        Stock("CIEB", "Credit Agricole Egypt", "بنك كريدي أجريكول مصر", "Banking"),
        Stock("FAISAL", "Faisal Islamic Bank of Egypt", "بنك فيصل الإسلامي المصري", "Banking"),
        Stock("SAIB", "Societe Arabe Internationale De Banque", "الشركة العربية الدولية للبنوك", "Banking"),
        Stock("ADIB", "Abu Dhabi Islamic Bank Egypt", "بنك أبوظبي الإسلامي مصر", "Banking"),
        Stock("EGBE", "Egyptian Gulf Bank", "البنك المصري الخليجي", "Banking"),
        Stock("HDBK", "Housing & Development Bank", "بنك التعمير والإسكان", "Banking"),
        
        // Real Estate
        Stock("TMGH", "Talaat Moustafa Group Holding", "مجموعة طلعت مصطفى القابضة", "Real Estate"),
        Stock("MNHD", "Madinet Nasr Housing and Development", "مدينة نصر للإسكان والتعمير", "Real Estate"),
        Stock("PHDC", "Palm Hills Developments", "بالم هيلز للتنمية", "Real Estate"),
        Stock("EMFD", "Emaar Misr for Development", "إعمار مصر للتنمية", "Real Estate"),
        Stock("OCDI", "Orascom Development Egypt", "أوراسكوم للتنمية مصر", "Real Estate"),
        Stock("AMER", "Amer Group Holding", "عامر جروب القابضة", "Real Estate"),
        Stock("HELI", "Heliopolis Company for Housing and Development", "مصر الجديدة للإسكان والتعمير", "Real Estate"),
        Stock("SODIC", "Six of October Development and Investment", "السادس من أكتوبر للتنمية والاستثمار", "Real Estate"),
        Stock("ORHD", "Orascom Development Egypt", "أورا للتطوير العقاري", "Real Estate"),
        
        // Food & Beverage
        Stock("JUFO", "Juhayna Food Industries", "جهينة للصناعات الغذائية", "Food & Beverage"),
        Stock("EAST", "Eastern Company", "الشرقية للدخان", "Food & Beverage"),
        Stock("ISMA", "Ismailia Misr Poultry", "الإسماعيلية مصر للدواجن", "Food & Beverage"),
        Stock("CANA", "Canal Sugar Company", "قناة السويس للسكر", "Food & Beverage"),
        Stock("MNPH", "Misr National Poultry", "القومية للدواجن", "Food & Beverage"),
        Stock("DOMTY", "Arabian Food Industries", "العربية للصناعات الغذائية", "Food & Beverage"),
        Stock("POUL", "Cairo Poultry Company", "القاهرة للدواجن", "Food & Beverage"),
        Stock("DSCW", "Delta Sugar Company", "الدلتا للسكر", "Food & Beverage"),
        Stock("SUGR", "Delta Sugar Company", "الدلتا للسكر", "Food & Beverage"),
        
        // Building Materials & Construction
        Stock("SWDY", "Elsewedy Electric", "السويدي اليكتريك", "Building Materials"),
        Stock("ARCC", "Arabian Cement Company", "أسمنت العربية", "Building Materials"),
        Stock("SVCE", "Suez Cement Company", "السويس للأسمنت", "Building Materials"),
        Stock("PRCL", "Porcelain Cleopatra", "كليوباترا للسيراميك", "Building Materials"),
        Stock("EGCH", "Egyptian Chemical Industries", "الكيماويات المصرية", "Building Materials"),
        Stock("SPIN", "Sinai Cement", "سيناء للأسمنت", "Building Materials"),
        Stock("ELRS", "Ezz Steel", "حديد عز", "Building Materials"),
        Stock("ESRS", "Ezz Steel", "حديد عز", "Building Materials"),
        Stock("ALUM", "Egypt Aluminum", "مصر للألومنيوم", "Building Materials"),
        
        // Oil & Gas
        Stock("AMOC", "Alexandria Mineral Oils Company", "اسكندرية للزيوت المعدنية", "Oil & Gas"),
        Stock("MOPCO", "Misr Fertilizers Production Company", "موبكو", "Oil & Gas"),
        Stock("SKPC", "Sidi Kerir Petrochemicals", "سيدي كرير للبتروكيماويات", "Oil & Gas"),
        Stock("ACGC", "Alexandria Container and Cargo Handling", "اسكندرية للحاويات والبضائع", "Oil & Gas"),
        
        // Financial Services
        Stock("EFID", "Edita Food Industries", "إديتا للصناعات الغذائية", "Financial Services"),
        Stock("HRHO", "EFG Holding", "إي أف جي القابضة", "Financial Services"),
        Stock("CIHE", "CI Capital Holding", "سي آي كابيتال القابضة", "Financial Services"),
        Stock("EKHOA", "Egypt Kuwait Holding", "المصرية الكويتية القابضة", "Financial Services"),
        Stock("EXPA", "Export Development Bank of Egypt", "البنك المصري لتنمية الصادرات", "Financial Services"),
        
        // Telecommunications
        Stock("ETEL", "Telecom Egypt", "المصرية للاتصالات", "Telecommunications"),
        Stock("ORWE", "Orange Egypt", "اورنج مصر", "Telecommunications"),
        Stock("EMOB", "E-Finance", "إي فاينانس", "Telecommunications"),
        
        // Healthcare & Pharmaceuticals
        Stock("DCRC", "Delta Pharma", "دلتا فارما", "Healthcare"),
        Stock("EIPH", "Egyptian International Pharmaceutical", "الدوائية المصرية الدولية", "Healthcare"),
        Stock("PHAR", "Pharos Holding", "فاروس القابضة", "Healthcare"),
        Stock("ADCI", "Alexandria Medical Services", "الاسكندرية للخدمات الطبية", "Healthcare"),
        Stock("CLHO", "Cleopatra Hospital Group", "مجموعة كليوباترا للمستشفيات", "Healthcare"),
        Stock("IBAG", "Ibn Sina Pharma", "ابن سينا فارما", "Healthcare"),
        Stock("MFPC", "Misr Fertilizers Production Company", "موبكو", "Oil & Gas"),
        Stock("EPCO", "Egyptian Pharma", "المصرية للأدوية والكيماويات", "Healthcare"),
        
        // Industrial
        Stock("EGAL", "Egypt Aluminum", "مصر للألومنيوم", "Industrial"),
        Stock("AUTO", "GB Auto", "جي بي أوتو", "Industrial"),
        Stock("ELKA", "El Kabel", "مصنع الكابلات الكهربائية", "Industrial"),
        Stock("ECAP", "Egypt Caps", "ايجيكاب للأغطية", "Industrial"),
        Stock("UEGC", "Upper Egypt Contracting", "المقاولون العرب", "Industrial"),
        Stock("ENGC", "Engineering Industries", "الهندسية للصناعات", "Industrial"),
        
        // Textiles & Apparel
        Stock("ACGC", "Alexandria Container and Cargo Handling", "الاسكندرية للحاويات", "Textiles"),
        Stock("ORAS", "Orascom Construction", "أوراسكوم كونستراكشن", "Textiles"),
        Stock("ELGD", "El Nasr Clothes & Textiles", "النصر للملابس والمنسوجات", "Textiles"),
        
        // Tourism & Hotels
        Stock("OCDI", "Orascom Development Egypt", "أوراسكوم للتنمية مصر", "Tourism"),
        Stock("EKHO", "Egypt Kuwait Holding", "المصرية الكويتية القابضة", "Tourism"),
        Stock("MTIE", "Misr Tourism", "مصر للسياحة", "Tourism"),
        
        // Transportation & Logistics
        Stock("ALCN", "Alexandria Container and Cargo Handling", "اسكندرية للحاويات", "Transportation"),
        Stock("CCRS", "Cairo Transport", "القاهرة للنقل", "Transportation"),
        Stock("EGTS", "Egyptian Transport", "مصر للنقل", "Transportation"),
        
        // Diversified Holdings
        Stock("ABUK", "Abou Kir Fertilizers", "أبو قير للأسمدة", "Diversified"),
        Stock("BONY", "Bonyan for Development and Trade", "بنيان للتطوير والتجارة", "Real Estate"),
        Stock("MICH", "Misr Chemical Industries", "مصر لصناعة الكيماويات", "Diversified"),
        Stock("OREG", "Oriental Weavers", "السجاد الشرقي", "Diversified"),
        Stock("EKHO", "Egypt Kuwait Holding", "المصرية الكويتية", "Diversified"),
        Stock("SPMD", "Speed Medical", "سبيد ميديكال", "Healthcare"),
        Stock("ASCM", "Arabian Cement Company", "أسمنت العربية", "Building Materials"),
        Stock("KIMA", "Abou Kir Fertilizers", "أبو قير للأسمدة", "Industrial"),
        Stock("GPCP", "Giza General Contracting", "الجيزة العامة للمقاولات", "Industrial"),
        Stock("NCGC", "National Cement", "الاهلي للأسمنت", "Building Materials"),
        Stock("PRSC", "Premier Services", "بريميير للخدمات", "Services"),
        Stock("ATQA", "Atqa Cement", "أسمنت أطقا", "Building Materials"),
        Stock("EGSA", "Egypt Sat", "ايجيسات", "Technology"),
        Stock("MODI", "Modern Dairy", "الألبان الحديثة", "Food & Beverage"),
        Stock("MPRC", "Middle East Packaging", "الشرق الأوسط للتعبئة", "Industrial"),
        Stock("RTVC", "El Nasr Transformers", "النصر للمحولات", "Industrial"),
        Stock("MTIE", "Misr Tourism Investment", "مصر للاستثمارات السياحية", "Tourism"),
        Stock("MNCC", "Misr National Cement", "الوطنية للأسمنت", "Building Materials")
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
