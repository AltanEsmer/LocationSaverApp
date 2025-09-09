# 🎫 Bilet: Konum Kaydetme Uygulaması (Kotlin - Android)

## 🎯 Amaç
Kişisel kullanım için Android uygulaması geliştirmek.  
Uygulama şunları yapmalı:
- Kullanıcı konumunu alabilmeli
- "Save Location" ile isim vererek kaydedebilmeli
- Kaydedilen konumları listeleyebilmeli
- Listedeki konuma tıklayınca Google Maps ile yönlendirme açabilmeli
- (Opsiyonel Faz 3) Arka planda sürekli konum kaydı yapabilmeli (Foreground Service)

APK olarak build edilip arkadaşın Android cihazında çalıştırılacak.  

---

## 🔄 Geliştirme Akışı

### Faz 0 – Proje Kurulumu
- Android Studio ile boş **Kotlin** projesi oluştur (Empty Activity).
- Minimum SDK: Android 8.0 (API 26).
- Cursor’da açılacak dosya yapısı hazır:
app/
└── src/main/java/com/example/locationapp/
├── MainActivity.kt
├── data/
├── ui/
└── service/

markdown
Kodu kopyala
- Git repo oluştur ve `.gitignore`, `README.md`, `LICENSE` ekle.

### Faz 1 – Konum Alma & Kaydetme
- `FusedLocationProviderClient` kullanarak anlık konum al.
- `Room` ile SQLite tabanlı DB kur:
- `LocationEntity(id, name, latitude, longitude, timestamp)`
- `LocationDao` → insert, getAll
- UI:  
- "Save Location" butonu  
- Kaydetmeden önce kullanıcıya isim sormak için `AlertDialog`  
- Kayıt sonrası DB’ye yaz.

### Faz 2 – Listeleme & Maps
- Yeni ekran (Activity/Fragment) → kayıtlı konumları listele (RecyclerView veya Jetpack Compose).
- Her kayıt:
- İsim + tarih göster.
- Tıklanınca **Google Maps intent** ile aç:
  ```kotlin
  val gmmIntentUri = Uri.parse("geo:${latitude},${longitude}?q=${latitude},${longitude}(${name})")
  val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
  mapIntent.setPackage("com.google.android.apps.maps")
  startActivity(mapIntent)
  ```

### Faz 3 – Arka Plan Konum Takibi (Opsiyonel)
- Foreground Service oluştur.
- Belirli aralıklarla konum alıp DB’ye kaydet.
- Notification göster (Android 10+ gereksinim).
- Pil tüketimine dikkat et → konfigürasyonla kullanıcı ayarına bırak.

---

## 📦 Build & Yayınlama
- Cursor’da kod yaz → Android Studio’da build et.
- APK üretmek için:
- Menü: `Build > Build APK(s)`
- Çıktı: `app/build/outputs/apk/debug/app-debug.apk`
- Bu dosya arkadaşına gönderilebilir.

---

## ⚠️ Cursor için Dikkat Notları
1. **Gradle & Manifest dosyaları**  
 - Cursor bu dosyaları değiştirebilir ama yapıyı bozma.  
 - Özellikle `app/build.gradle` ve `AndroidManifest.xml` kritik.  

2. **Resource klasörü (`res/`)**  
 - XML layout, drawable, strings burada → Cursor düzenlerken yanlışlıkla silmemeli.  

3. **Kod odaklı çalış**  
 - Cursor’u sadece `java/com/...` altındaki Kotlin kodlarında aktif kullan.  

4. **Test**  
 - Cursor’da sadece kod yaz → build/run için Android Studio kullan.  
 - Her önemli değişiklikten sonra Android Studio’da build alıp emulatorda test et.  

---

## ✅ Kabul Kriterleri
- [ ] Uygulama açılıyor ve crash olmuyor.
- [ ] "Save Location" çalışıyor ve kayıt DB’ye ekleniyor.
- [ ] Kayıtlı konumlar listeleniyor.
- [ ] Konuma tıklandığında Google Maps açılıyor.
- [ ] APK üretilebiliyor ve arkadaşın cihazında çalışıyor.
