# APKMirror URL Mapping Generator

Bu script, Gemini AI kullanarak popüler Android uygulamaları için APKMirror URL mapping'lerini otomatik olarak oluşturur.

## Neden Gerekli?

APKMirror, package name ile doğrudan uygulama sayfasına gitmeyi desteklemiyor. URL formatı şöyle:

```
https://www.apkmirror.com/apk/{publisher-slug}/{app-slug}/
```

Örnek:
- WhatsApp → `whatsapp-inc/whatsapp-messenger`
- Instagram → `instagram/instagram`
- YouTube → `google-inc/youtube`

Bu bilgiler package name'den (örn: `com.whatsapp`) otomatik olarak çıkarılamıyor. Bu script, Gemini AI kullanarak bu mapping'leri oluşturur.

---

## 📋 Adım Adım Kurulum Rehberi

### Adım 1: Python Kontrolü

Önce Python'un yüklü olduğundan emin olun:

```bash
python3 --version
```

Eğer yüklü değilse:
- **Windows**: https://www.python.org/downloads/ adresinden indirin
- **Mac**: `brew install python3`
- **Linux**: `sudo apt install python3 python3-pip`

### Adım 2: Google AI Studio'dan API Anahtarı Alma

1. **Google AI Studio'ya gidin:**
   
   🔗 https://aistudio.google.com/app/apikey

2. **Google hesabınızla giriş yapın**

3. **"Create API Key" butonuna tıklayın:**
   
   ![Create API Key](https://i.imgur.com/placeholder.png)
   
   - Sayfanın sol tarafında veya ortasında "Create API Key" veya "Get API key" butonu olacak
   - Tıklayın

4. **Proje seçin veya oluşturun:**
   
   - "Create API key in new project" seçeneğini seçebilirsiniz
   - Veya mevcut bir Google Cloud projenizi seçin

5. **API Anahtarını kopyalayın:**
   
   - Oluşan anahtar şöyle görünecek: `AIzaSyC...` (yaklaşık 39 karakter)
   - **Bu anahtarı güvenli bir yere kaydedin!**
   - Anahtarı kimseyle paylaşmayın

### Adım 3: Gerekli Paketi Yükleyin

Terminal/Komut İstemi'nde şu komutu çalıştırın:

```bash
pip install google-generativeai
```

veya

```bash
pip3 install google-generativeai
```

### Adım 4: Repoyu Klonlayın (Eğer henüz yapmadıysanız)

```bash
git clone https://github.com/GloriousTR/OmniAPK.git
cd OmniAPK
```

### Adım 5: API Anahtarını Ayarlayın

**Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY="BURAYA_API_ANAHTARINIZI_YAZIN"
```

**Windows (CMD):**
```cmd
set GEMINI_API_KEY=BURAYA_API_ANAHTARINIZI_YAZIN
```

**Mac/Linux:**
```bash
export GEMINI_API_KEY="BURAYA_API_ANAHTARINIZI_YAZIN"
```

### Adım 6: Script'i Çalıştırın

```bash
cd scripts
python3 generate_apkmirror_mapping.py
```

veya Windows'ta:

```bash
cd scripts
python generate_apkmirror_mapping.py
```

---

## 📤 Çıktı Örneği

Script başarılı çalışırsa şöyle bir çıktı göreceksiniz:

```
============================================================
🚀 APKMirror URL Mapping Generator
============================================================

📱 75 uygulama için mapping oluşturulacak...

[1/75] Tumblr (com.tumblr)... ✅ tumblr-inc/tumblr
[2/75] VK (com.vkontakte.android)... ✅ vk-com/vk
[3/75] WeChat (com.wechat)... ✅ wechat/wechat
...

============================================================
📊 SONUÇLAR
============================================================
✅ Başarılı: 68
❌ Başarısız: 7

============================================================
📋 Kotlin Kodu (APKMirrorUrlHelper.kt'ye ekleyin)
============================================================

// Gemini ile oluşturulan yeni mapping'ler
        "com.tumblr" to "tumblr-inc/tumblr",
        "com.vkontakte.android" to "vk-com/vk",
        ...
```

**Bu "Kotlin Kodu" bölümündeki çıktıyı bana paylaşın, ben uygulamaya ekleyeyim!**

---

## 🔧 Yeni Uygulama Ekleme

Eğer farklı uygulamalar için mapping oluşturmak isterseniz, `generate_apkmirror_mapping.py` dosyasındaki `APPS_TO_ADD` listesini düzenleyebilirsiniz:

```python
APPS_TO_ADD = [
    ("com.example.app", "Example App"),
    ("com.another.app", "Another App"),
    # Format: (package_name, uygulama_adi)
]
```

---

## ⚠️ Sorun Giderme

### "GEMINI_API_KEY ortam değişkeni ayarlanmamış" hatası

API anahtarını doğru ayarladığınızdan emin olun. Terminal'i kapatıp açtıysanız, `export` komutunu tekrar çalıştırmanız gerekir.

### "google-generativeai paketi yüklü değil" hatası

```bash
pip install google-generativeai
```

### "API hatası" mesajları

- API anahtarınızın doğru olduğundan emin olun
- İnternet bağlantınızı kontrol edin
- Google AI Studio'da API anahtarının aktif olduğunu kontrol edin

---

## 📁 Çıktı Dosyaları

- **apkmirror_new_mappings.json**: Oluşan mapping'lerin JSON formatı
- **Konsol çıktısı**: Kotlin kodu formatında mapping'ler (bunu bana paylaşın!)

---

## 🔒 Güvenlik Notu

- API anahtarınızı GitHub'a veya başka bir yere yüklemeyin
- API anahtarını başkalarıyla paylaşmayın
- Script çıktısını paylaşırken API anahtarının görünmediğinden emin olun
