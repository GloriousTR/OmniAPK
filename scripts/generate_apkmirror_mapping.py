#!/usr/bin/env python3
"""
APKMirror URL Mapping Generator using Gemini API

Bu script, popüler Android uygulamaları için APKMirror URL mapping'lerini
Gemini AI kullanarak otomatik olarak oluşturur.

Kullanım:
1. Gemini API anahtarınızı ayarlayın:
   export GEMINI_API_KEY="your-api-key"

2. Script'i çalıştırın:
   python3 generate_apkmirror_mapping.py

3. Oluşan mapping'i APKMirrorUrlHelper.kt dosyasına kopyalayın

Gemini API Key almak için: https://aistudio.google.com/app/apikey
"""

import os
import json
import time
import re
from typing import Optional

try:
    import google.generativeai as genai
    GEMINI_AVAILABLE = True
except ImportError:
    GEMINI_AVAILABLE = False
    print("⚠️  google-generativeai paketi yüklü değil.")
    print("   Yüklemek için: pip install google-generativeai")

# Mapping'e eklenecek yeni uygulamalar listesi
# İstediğiniz uygulamaları buraya ekleyebilirsiniz
APPS_TO_ADD = [
    # Sosyal Medya
    ("com.tumblr", "Tumblr"),
    ("com.vkontakte.android", "VK"),
    ("com.wechat", "WeChat"),
    ("tv.periscope.android", "Periscope"),
    ("com.duolingo", "Duolingo"),
    
    # Müzik & Podcast
    ("com.pandora.android", "Pandora"),
    ("com.audible.application", "Audible"),
    ("fm.castbox.audiobook.radio.podcast", "Castbox"),
    ("com.deezer.android.app", "Deezer"),
    ("com.apple.android.music", "Apple Music"),
    
    # Alışveriş
    ("com.shopee.id", "Shopee"),
    ("com.lazada.android", "Lazada"),
    ("com.etsy.android", "Etsy"),
    ("com.target.ui", "Target"),
    ("com.walmart.android", "Walmart"),
    
    # Finans
    ("com.venmo", "Venmo"),
    ("com.binance.dev", "Binance"),
    ("com.coinbase.android", "Coinbase"),
    ("com.robinhood.android", "Robinhood"),
    ("com.squareup.cash", "Cash App"),
    
    # Seyahat
    ("com.booking", "Booking.com"),
    ("com.airbnb.android", "Airbnb"),
    ("com.expedia.bookings", "Expedia"),
    ("com.ubercab", "Uber"),
    ("com.lyft.android", "Lyft"),
    
    # Haberler
    ("com.twitter.android.lite", "Twitter Lite"),
    ("flipboard.app", "Flipboard"),
    ("com.nytimes.android", "NY Times"),
    ("com.cnn.mobile.android.phone", "CNN"),
    ("com.google.android.apps.magazines", "Google News"),
    
    # Fotoğraf & Video
    ("com.vsco.cam", "VSCO"),
    ("com.picsart.studio", "PicsArt"),
    ("video.like", "Likee"),
    ("com.ss.android.ugc.aweme", "TikTok Global"),
    ("com.google.android.apps.youtube.creator", "YouTube Studio"),
    
    # Oyunlar
    ("com.activision.callofduty.shooter", "Call of Duty Mobile"),
    ("com.tencent.ig", "PUBG Mobile"),
    ("com.garena.game.ffsea", "Free Fire"),
    ("com.epicgames.fortnite", "Fortnite"),
    ("com.innersloth.spacemafia", "Among Us"),
    ("com.dts.freefireth", "Free Fire MAX"),
    ("com.ea.gp.fifamobile", "FIFA Mobile"),
    ("com.riotgames.league.wildrift", "Wild Rift"),
    
    # Sağlık & Fitness
    ("com.strava", "Strava"),
    ("com.myfitnesspal.android", "MyFitnessPal"),
    ("com.calm.android", "Calm"),
    ("com.headspace.android", "Headspace"),
    ("com.nike.plusgps", "Nike Run Club"),
    
    # Eğitim
    ("com.coursera.app", "Coursera"),
    ("com.udemy.android", "Udemy"),
    ("com.linkedin.android.learning", "LinkedIn Learning"),
    ("com.quizlet.quizletandroid", "Quizlet"),
    ("org.khanacademy.android", "Khan Academy"),
    
    # Araçlar
    ("com.google.android.inputmethod.latin", "Gboard"),
    ("com.touchtype.swiftkey", "SwiftKey"),
    ("com.grammarly.android.keyboard", "Grammarly"),
    ("com.lastpass.lpandroid", "LastPass"),
    ("com.onepassword.android", "1Password"),
    ("org.torproject.torbrowser", "Tor Browser"),
]


def setup_gemini(api_key: str) -> bool:
    """Gemini API'yi yapılandırır"""
    if not GEMINI_AVAILABLE:
        return False
    try:
        genai.configure(api_key=api_key)
        return True
    except Exception as e:
        print(f"❌ Gemini API yapılandırma hatası: {e}")
        return False


def get_apkmirror_mapping(package_name: str, app_name: str) -> Optional[str]:
    """
    Gemini kullanarak bir uygulama için APKMirror URL mapping'i oluşturur.
    
    Returns:
        "publisher/app-slug" formatında mapping veya None
    """
    if not GEMINI_AVAILABLE:
        return None
        
    model = genai.GenerativeModel('gemini-1.5-flash')
    
    prompt = f"""You are an expert on APKMirror website URL structure.

APKMirror uses this URL format: https://www.apkmirror.com/apk/[publisher-slug]/[app-slug]/

Given the app information below, provide ONLY the "publisher-slug/app-slug" part.
Do NOT include the full URL, just the path segment.

App Name: {app_name}
Package Name: {package_name}

Rules:
- Publisher slug is usually the company name in lowercase with hyphens (e.g., "google-inc", "facebook-2", "whatsapp-inc")
- App slug is the app name in lowercase with hyphens (e.g., "whatsapp-messenger", "instagram", "youtube")
- Some publishers have numbers (e.g., facebook-2)
- Return ONLY the path like: publisher-slug/app-slug
- If you're not sure, return "UNKNOWN"

Response (just the path, nothing else):"""

    try:
        response = model.generate_content(prompt)
        result = response.text.strip()
        
        # Validate the response format
        if "/" in result and result != "UNKNOWN" and len(result.split("/")) == 2:
            # Remove any quotes or extra characters
            result = result.replace('"', '').replace("'", "").strip()
            # Validate it looks like a valid slug
            if re.match(r'^[a-z0-9\-]+/[a-z0-9\-]+$', result):
                return result
        
        return None
    except Exception as e:
        print(f"  ⚠️  API hatası: {e}")
        return None


def generate_kotlin_mapping(mappings: dict) -> str:
    """Kotlin kodu formatında mapping oluşturur"""
    lines = []
    
    for package_name, slug in sorted(mappings.items()):
        lines.append(f'        "{package_name}" to "{slug}",')
    
    return "\n".join(lines)


def main():
    print("=" * 60)
    print("🚀 APKMirror URL Mapping Generator")
    print("=" * 60)
    
    # API key kontrolü
    api_key = os.environ.get("GEMINI_API_KEY")
    
    if not api_key:
        print("\n❌ GEMINI_API_KEY ortam değişkeni ayarlanmamış!")
        print("\nKullanım:")
        print("  1. Gemini API anahtarınızı alın:")
        print("     https://aistudio.google.com/app/apikey")
        print("\n  2. Ortam değişkenini ayarlayın:")
        print("     export GEMINI_API_KEY='your-api-key-here'")
        print("\n  3. Script'i tekrar çalıştırın:")
        print("     python3 generate_apkmirror_mapping.py")
        return
    
    if not GEMINI_AVAILABLE:
        print("\n❌ google-generativeai paketi yüklü değil!")
        print("   Yüklemek için: pip install google-generativeai")
        return
    
    if not setup_gemini(api_key):
        return
    
    print(f"\n📱 {len(APPS_TO_ADD)} uygulama için mapping oluşturulacak...\n")
    
    new_mappings = {}
    failed = []
    
    for i, (package_name, app_name) in enumerate(APPS_TO_ADD, 1):
        print(f"[{i}/{len(APPS_TO_ADD)}] {app_name} ({package_name})...", end=" ")
        
        mapping = get_apkmirror_mapping(package_name, app_name)
        
        if mapping:
            new_mappings[package_name] = mapping
            print(f"✅ {mapping}")
        else:
            failed.append((package_name, app_name))
            print("❌ Bulunamadı")
        
        # Rate limiting - API'yi yormamak için
        time.sleep(0.5)
    
    # Sonuçları göster
    print("\n" + "=" * 60)
    print("📊 SONUÇLAR")
    print("=" * 60)
    print(f"✅ Başarılı: {len(new_mappings)}")
    print(f"❌ Başarısız: {len(failed)}")
    
    if new_mappings:
        print("\n" + "=" * 60)
        print("📋 Kotlin Kodu (APKMirrorUrlHelper.kt'ye ekleyin)")
        print("=" * 60)
        print("\n// Gemini ile oluşturulan yeni mapping'ler")
        print(generate_kotlin_mapping(new_mappings))
    
    if failed:
        print("\n" + "=" * 60)
        print("⚠️  Başarısız Uygulamalar (manuel kontrol gerekiyor)")
        print("=" * 60)
        for package_name, app_name in failed:
            print(f"  - {app_name} ({package_name})")
    
    # JSON olarak da kaydet
    output_file = "apkmirror_new_mappings.json"
    with open(output_file, "w") as f:
        json.dump(new_mappings, f, indent=2)
    print(f"\n💾 Mapping'ler '{output_file}' dosyasına kaydedildi.")


if __name__ == "__main__":
    main()
