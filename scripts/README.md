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

## Kurulum

1. **Python bağımlılıklarını yükleyin:**

```bash
pip install google-generativeai
```

2. **Gemini API anahtarı alın:**
   - [Google AI Studio](https://aistudio.google.com/app/apikey) adresine gidin
   - "Create API Key" butonuna tıklayın
   - Oluşan anahtarı kopyalayın

3. **API anahtarını ayarlayın:**

```bash
export GEMINI_API_KEY="your-api-key-here"
```

## Kullanım

1. **Script'i çalıştırın:**

```bash
cd scripts
python3 generate_apkmirror_mapping.py
```

2. **Çıktıyı kopyalayın:**

Script, Kotlin formatında mapping kodu üretecek:

```kotlin
// Gemini ile oluşturulan yeni mapping'ler
"com.booking" to "booking-com-b-v/booking-com-hotels-apartments",
"com.ubercab" to "uber-technologies-inc/uber-request-a-ride",
...
```

3. **APKMirrorUrlHelper.kt dosyasına ekleyin:**

`app/src/main/java/com/aurora/store/util/APKMirrorUrlHelper.kt` dosyasındaki `packageMappings` map'ine yeni satırları ekleyin.

## Yeni Uygulama Ekleme

Script'teki `APPS_TO_ADD` listesine yeni uygulamalar ekleyebilirsiniz:

```python
APPS_TO_ADD = [
    ("com.example.app", "Example App"),
    ("com.another.app", "Another App"),
    # ...
]
```

## Notlar

- Gemini her zaman doğru sonuç vermeyebilir. Oluşan URL'leri manuel olarak kontrol etmeniz önerilir.
- APKMirror'da bulunmayan uygulamalar için mapping oluşturulamaz.
- Rate limiting için her istek arasında 0.5 saniye bekleme süresi vardır.

## Çıktı Dosyaları

- **apkmirror_new_mappings.json**: Oluşan mapping'lerin JSON formatı
- **Konsol çıktısı**: Kotlin kodu formatında mapping'ler
