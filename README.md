# Auth Performans Karşılaştırması

🌍 **[Read in English](README_EN.md)**

Bu proje, Spring Boot kullanarak 3 farklı kimlik doğrulama (Authentication) stratejisinin performansını karşılaştırmak amacıyla hazırlanmıştır. Amaç, veritabanı sorgularının, Redis önbelleklemesinin ve Stateless (Durumsuz) JWT kullanımının sistem gecikmesi (latency) ve kapasitesi (RPS) üzerindeki etkisini canlı olarak gözlemlemektir.

## 🚀 Stratejiler

Proje 3 farklı senaryoyu test eder:

1.  **Veritabanı (Database) Yaklaşımı** (`/api/v1/test/db`)
    *   Her istekte JWT'den `userId` okunur.
    *   Veritabanına (`users` tablosu) sorgu atılarak kullanıcı doğrulanır.
    *   **Avantaj:** En güncel veri.
    *   **Dezavantaj:** Yüksek gecikme, veritabanı darboğazı.

2.  **Redis Cache Yaklaşımı** (`/api/v1/test/redis`)
    *   Her istekte önce Redis'e bakılır.
    *   Eğer veri Redis'te yoksa (Cache Miss), veritabanından çekilip Redis'e yazılır.
    *   **Avantaj:** Veritabanına göre çok daha hızlıdır.
    *   **Dezavantaj:** Ekstra altyapı maliyeti ve cache invalidation yönetimi gerektirir.

3.  **Stateless (Durumsuz) JWT Yaklaşımı** (`/api/v1/test/stateless`)
    *   Token içerisinde `username`, `role` gibi bilgiler (Claims) taşınır.
    *   Sunucu hiçbir yere (DB veya Redis) sormadan sadece imza doğrulaması yapar.
    *   **Avantaj:** Sıfır Network I/O, en yüksek hız ve ölçeklenebilirlik.
    *   **Dezavantaj:** Token iptali (Revocation) zordur, token boyutu büyüyebilir.

## 🛠️ Kurulum ve Çalıştırma

### Gereksinimler
*   Java 17+
*   Docker & Docker Compose
*   K6 (Benchmark testi için)

### 1. Altyapıyı Ayağa Kaldırın
PostgreSQL ve Redis'i Docker üzerinde çalıştırmak için proje dizininde şu komutu çalıştırın:

```bash
docker-compose up -d
```
*   **Postgres:** Port `5438` (Kullanıcı: `postgres`, Şifre: `pass`)
*   **Redis:** Port `6388`

### 2. Uygulamayı Başlatın
Uygulama ilk açılışta veritabanına **10.000 test kullanıcısı** ekleyecektir.

```bash
mvn spring-boot:run
```
Uygulama `http://localhost:8080` adresinde çalışacaktır.

### 3. Test Tokenlarını Üretin
Test yapabilmek için geçerli JWT tokenlarına ihtiyacınız var. Tarayıcınızda şu adrese gidin:

`http://localhost:8080/api/v1/test/generate-tokens?userId=1`

Size dönen JSON içinde iki token olacak:
*   `minimal`: DB ve Redis testleri için (sadece ID içerir).
*   `full`: Stateless test için (rol ve email bilgilerini de içerir).

Bu tokenları kopyalayıp `perf-test.js` dosyasındaki ilgili yerlere yapıştırın (Otomatik olarak yapılmış olabilir, kontrol edin).

## 🧪 Performans Testi (Benchmark)

Test aracı olarak **K6** kullanıyoruz.

1.  Terminali proje dizininde açın.
2.  `perf-test.js` dosyasını açarak test etmek istediğiniz senaryonun önündeki yorum satırını kaldırın (`testDb`, `testRedis` veya `testStateless`).
3.  Testi başlatın:

```powershell
& "C:\Program Files\k6\k6.exe" run perf-test.js
```

### Gerçek Test Sonuçları (Local Environment)

Aşağıdaki sonuçlar, Docker üzerinde çalışan PostgreSQL/Redis ve yerel makinede çalışan K6 ile elde edilmiştir (50 VUs, 1m 40s):

| Strateji | RPS (İstek/Saniye) | Ortalama Gecikme (Latency) |
| :--- | :--- | :--- |
| **Stateless** | ~2,966 | 12.18ms |
| **Redis** | ~2,243 | 16.96ms |
| **Database** | ~1,365 | 29.00ms |

**Analiz:**
*   **Stateless**, veritabanı yöntemine göre **2 kattan fazla** istek işleyebilmektedir.
*   **Redis**, veritabanına göre yaklaşık **%65 performans artışı** sağlamıştır.
*   **Database** yöntemi en yavaş olanıdır, çünkü her istekte disk tabanlı bir okuma (veya database cache lookup) ve network I/O maliyeti vardır.

## 📂 Proje Yapısı

*   `src/main/java/.../controller/TestController.java`: Test endpoint'lerinin olduğu yer.
*   `src/main/java/.../util/JwtUtil.java`: Token üretme ve doğrulama mantığı.
*   `src/main/java/.../config/RedisConfig.java`: Redis bağlantı ayarları.
*   `perf-test.js`: K6 yük testi senaryo dosyası.
*   `docker-compose.yml`: Altyapı konfigürasyonu.
