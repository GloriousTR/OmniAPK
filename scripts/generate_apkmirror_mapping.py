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

# Mapping'e eklenecek yeni uygulamalar listesi - HER KATEGORİ 40 UYGULAMA
APPS_TO_ADD = [
    # ═══════════════════════════════════════════════════════════════════
    # SOSYAL MEDYA & İLETİŞİM (40 adet)
    # ═══════════════════════════════════════════════════════════════════
    ("com.whatsapp", "WhatsApp"),
    ("com.instagram.android", "Instagram"),
    ("com.facebook.katana", "Facebook"),
    ("com.facebook.orca", "Messenger"),
    ("com.google.android.youtube", "YouTube"),
    ("com.twitter.android", "X (Twitter)"),
    ("com.zhiliaoapp.musically", "TikTok"),
    ("org.telegram.messenger", "Telegram"),
    ("com.snapchat.android", "Snapchat"),
    ("com.discord", "Discord"),
    ("com.reddit.frontpage", "Reddit"),
    ("com.pinterest", "Pinterest"),
    ("com.linkedin.android", "LinkedIn"),
    ("tv.twitch.android.app", "Twitch"),
    ("com.tencent.mm", "WeChat"),
    ("com.viber.voip", "Viber"),
    ("jp.naver.line.android", "LINE"),
    ("com.skype.raider", "Skype"),
    ("com.vkontakte.android", "VK"),
    ("com.tumblr", "Tumblr"),
    ("com.threads.android", "Threads"),
    ("com.bereal.ft", "BeReal"),
    ("com.quora.android", "Quora"),
    ("kik.android", "Kik"),
    ("com.imo.android.imoim", "imo"),
    ("com.kakao.talk", "KakaoTalk"),
    ("com.tinder", "Tinder"),
    ("com.bumble.app", "Bumble"),
    ("com.badoo.mobile", "Badoo"),
    ("com.hinge.android", "Hinge"),
    ("com.okcupid.okcupid", "OkCupid"),
    ("com.match.android.matchmobile", "Match"),
    ("com.happn.app", "Happn"),
    ("com.grindrapp.android", "Grindr"),
    ("com.zhiliaoapp.musically.go", "TikTok Lite"),
    ("com.facebook.lite", "Facebook Lite"),
    ("com.instagram.lite", "Instagram Lite"),
    ("com.whatsapp.w4b", "WhatsApp Business"),
    ("com.facebook.pages.app", "Facebook Pages"),
    ("com.facebook.mlite", "Messenger Lite"),

    # ═══════════════════════════════════════════════════════════════════
    # MÜZİK & PODCAST & SES (40 adet)
    # ═══════════════════════════════════════════════════════════════════
    ("com.spotify.music", "Spotify"),
    ("com.google.android.apps.youtube.music", "YouTube Music"),
    ("com.apple.android.music", "Apple Music"),
    ("com.amazon.mp3", "Amazon Music"),
    ("com.soundcloud.android", "SoundCloud"),
    ("com.deezer.android.app", "Deezer"),
    ("com.pandora.android", "Pandora"),
    ("com.tidal.wave", "Tidal"),
    ("com.shazam.android", "Shazam"),
    ("com.audible.application", "Audible"),
    ("fm.castbox.audiobook.radio.podcast", "Castbox"),
    ("com.anghami", "Anghami"),
    ("com.audiomack", "Audiomack"),
    ("com.pocketcasts.android", "Pocket Casts"),
    ("com.gaana", "Gaana"),
    ("com.jio.media.jiobeats", "JioSaavn"),
    ("com.bsbportal.music", "Wynk Music"),
    ("com.clearchannel.iheartradio.controller", "iHeartRadio"),
    ("tunein.player", "TuneIn Radio"),
    ("com.aspiro.tidal", "TIDAL"),
    ("com.mixcloud.player", "Mixcloud"),
    ("com.bandcamp.android", "Bandcamp"),
    ("com.musi", "Musi"),
    ("com.smule.singandroid", "Smule"),
    ("com.starmakerinteractive.starmaker", "StarMaker"),
    ("com.spotify.lite", "Spotify Lite"),
    ("com.boomplay.music", "Boomplay"),
    ("ru.yandex.music", "Yandex Music"),
    ("com.qobuz.music", "Qobuz"),
    ("com.free.music.player.mp3.player", "Music Player"),
    ("com.musixmatch.android.lyrify", "Musixmatch"),
    ("com.sonos.acr2", "Sonos"),
    ("com.soundhound.android.core", "SoundHound"),
    ("com.djit.apps.edjing.pro", "edjing Mix"),
    ("com.noisli.noisli", "Noisli"),
    ("com.rubycell.podcast.republic", "Podcast Republic"),
    ("com.stitcher.app", "Stitcher"),
    ("com.podcast.podcasts", "Google Podcasts"),
    ("com.overcast.podcast.player", "Overcast"),
    ("com.spreaker.android.studio", "Spreaker"),

    # ═══════════════════════════════════════════════════════════════════
    # ALIŞVERİŞ & E-TİCARET (40 adet)
    # ═══════════════════════════════════════════════════════════════════
    ("com.amazon.mShop.android.shopping", "Amazon Shopping"),
    ("com.ebay.mobile", "eBay"),
    ("com.alibaba.aliexpresshd", "AliExpress"),
    ("com.shopee.id", "Shopee"),
    ("com.lazada.android", "Lazada"),
    ("com.wish.android", "Wish"),
    ("com.shein.android", "SHEIN"),
    ("com.temu.app", "Temu"),
    ("com.walmart.android", "Walmart"),
    ("com.target.ui", "Target"),
    ("com.etsy.android", "Etsy"),
    ("com.costco.app.android", "Costco"),
    ("com.bestbuy.android", "Best Buy"),
    ("com.nike.omega", "Nike"),
    ("com.adidas.app", "Adidas"),
    ("com.hm.goe", "H&M"),
    ("com.zara.zara", "Zara"),
    ("com.sephora", "Sephora"),
    ("com.ulta.ulta", "Ulta Beauty"),
    ("com.ikea.kompan", "IKEA"),
    ("com.homedepot", "Home Depot"),
    ("com.lowes.android", "Lowe's"),
    ("com.wayfair.wayfair", "Wayfair"),
    ("com.macys.android", "Macy's"),
    ("com.nordstrom.app", "Nordstrom"),
    ("com.kohls.mcommerce.opal", "Kohl's"),
    ("com.jcpenney.android", "JCPenney"),
    ("com.samsclub.mobile", "Sam's Club"),
    ("com.walgreens.android", "Walgreens"),
    ("com.cvs.launchers.cvs", "CVS"),
    ("com.kroger.mobile", "Kroger"),
    ("com.target.ui", "Target"),
    ("com.mercari", "Mercari"),
    ("com.poshmark.app", "Poshmark"),
    ("com.offerup", "OfferUp"),
    ("com.craigslist.craigslistmobile", "Craigslist"),
    ("com.letgo.an", "Letgo"),
    ("com.dhgate.buyerapp", "DHgate"),
    ("com.banggood.client", "Banggood"),
    ("com.gearbest.en", "Gearbest"),

    # ═══════════════════════════════════════════════════════════════════
    # FİNANS & KRİPTO & BANKACILIK (40 adet)
    # ═══════════════════════════════════════════════════════════════════
    ("com.paypal.android.p2pmobile", "PayPal"),
    ("com.venmo", "Venmo"),
    ("com.squareup.cash", "Cash App"),
    ("com.google.android.apps.walletnfcrel", "Google Wallet"),
    ("com.apple.android.wallet", "Apple Wallet"),
    ("com.binance.dev", "Binance"),
    ("com.coinbase.android", "Coinbase"),
    ("com.robinhood.android", "Robinhood"),
    ("com.revolut.revolut", "Revolut"),
    ("com.wise.android", "Wise"),
    ("com.crypto.exchange", "Crypto.com"),
    ("com.trustwallet.app", "Trust Wallet"),
    ("com.wallet.crypto.trustapp", "Trust"),
    ("io.metamask", "MetaMask"),
    ("com.bybit.app", "Bybit"),
    ("com.okex.android", "OKX"),
    ("com.kucoin.market", "KuCoin"),
    ("com.kraken.trade", "Kraken"),
    ("piuk.blockchain.android", "Blockchain.com"),
    ("com.bitfinex.mobileapp", "Bitfinex"),
    ("exodusmovement.exodus", "Exodus"),
    ("com.ledger.live", "Ledger Live"),
    ("com.plaid.link", "Plaid"),
    ("com.mint", "Mint"),
    ("com.intuit.mint", "Mint"),
    ("com.wf.wellsfargomobile", "Wells Fargo"),
    ("com.chase.sig.android", "Chase"),
    ("com.citi.citimobile", "Citi"),
    ("com.americanexpress.android.acctsvcs.us", "American Express"),
    ("com.usaa.mobile.android.usaa", "USAA"),
    ("com.ally.MobileBank", "Ally Bank"),
    ("com.capitalone.mobile", "Capital One"),
    ("com.discover.mobile", "Discover"),
    ("com.moneylion.android", "MoneyLion"),
    ("com.sofi.mobile", "SoFi"),
    ("com.chime.themis", "Chime"),
    ("com.zellepay.zelle", "Zelle"),
    ("com.transferwise.android", "Wise"),
    ("com.remitly.remitly", "Remitly"),
    ("com.moneygram.mobile", "MoneyGram"),

    # ═══════════════════════════════════════════════════════════════════
    # SEYAHAT & ULAŞIM & HARİTA (40 adet)
    # ═══════════════════════════════════════════════════════════════════
    ("com.google.android.apps.maps", "Google Maps"),
    ("com.waze", "Waze"),
    ("com.ubercab", "Uber"),
    ("com.lyft.android", "Lyft"),
    ("com.booking", "Booking.com"),
    ("com.airbnb.android", "Airbnb"),
    ("com.expedia.bookings", "Expedia"),
    ("com.tripadvisor.tripadvisor", "TripAdvisor"),
    ("com.skyscanner.android.main", "Skyscanner"),
    ("com.kayak.android", "Kayak"),
    ("com.trivago", "Trivago"),
    ("com.hotels.android", "Hotels.com"),
    ("com.grab.passenger", "Grab"),
    ("bolt.driver", "Bolt"),
    ("com.flightradar24free", "Flightradar24"),
    ("com.google.android.apps.travel.onthego", "Google Travel"),
    ("com.hostelworld.app", "Hostelworld"),
    ("com.vrbo.android", "Vrbo"),
    ("com.agoda.mobile.consumer", "Agoda"),
    ("com.priceline.android.negotiator", "Priceline"),
    ("com.hopper.mountainview.play", "Hopper"),
    ("com.aa.android", "American Airlines"),
    ("com.delta.mobile.android", "Delta"),
    ("com.united.mobile.android", "United"),
    ("com.southwest.android", "Southwest"),
    ("com.ryanair.ryanair", "Ryanair"),
    ("uk.co.easyjet.mapp", "easyJet"),
    ("com.lufthansa.android", "Lufthansa"),
    ("com.emirates.ek.android", "Emirates"),
    ("com.qatarairways.qmobapp", "Qatar Airways"),
    ("com.turkishairlines.mobile", "Turkish Airlines"),
    ("citymapper.citymapper", "Citymapper"),
    ("com.sygic.aura", "Sygic"),
    ("com.here.app.maps", "HERE WeGo"),
    ("com.mapswithme.maps.pro", "MAPS.ME"),
    ("com.tomtom.gplay.navapp", "TomTom"),
    ("com.inrix.android.ui", "INRIX"),
    ("com.gasbuddy.android", "GasBuddy"),
    ("me.lyft.driver", "Lyft Driver"),
    ("com.ubercab.driver", "Uber Driver"),

    # ═══════════════════════════════════════════════════════════════════
    # HABERLER & OKUMA & MEDYA (40 adet)
    # ═══════════════════════════════════════════════════════════════════
    ("com.google.android.apps.magazines", "Google News"),
    ("flipboard.app", "Flipboard"),
    ("com.nytimes.android", "NY Times"),
    ("com.cnn.mobile.android.phone", "CNN"),
    ("com.bbc.news", "BBC News"),
    ("com.guardian", "The Guardian"),
    ("com.foxnews.android", "Fox News"),
    ("com.washingtonpost.android", "Washington Post"),
    ("com.reuters.news", "Reuters"),
    ("com.medium.reader", "Medium"),
    ("com.news.us.agenda.media.washington.times", "Washington Times"),
    ("com.espn.score_center", "ESPN"),
    ("com.bleacherreport.android.teamstream", "Bleacher Report"),
    ("com.theScore", "theScore"),
    ("com.yahoo.mobile.client.android.yahoo", "Yahoo"),
    ("com.msn.news", "Microsoft News"),
    ("com.news.bing", "Bing News"),
    ("com.buzzfeed.android", "BuzzFeed"),
    ("com.vice.android", "VICE"),
    ("com.huffingtonpost.android", "HuffPost"),
    ("au.com.newscorp.newsnetwork", "News Corp"),
    ("com.usatoday.android.news", "USA Today"),
    ("com.latimes", "LA Times"),
    ("com.sfgate.sfnews", "SFGate"),
    ("com.politico.android", "Politico"),
    ("com.axios.android", "Axios"),
    ("com.substack.android", "Substack"),
    ("com.reddit.frontpage", "Reddit"),
    ("com.instapaper.android", "Instapaper"),
    ("com.ideashower.readitlater.pro", "Pocket"),
    ("com.feedly.android.core", "Feedly"),
    ("com.inoreader.android", "Inoreader"),
    ("com.newsblur.android", "NewsBlur"),
    ("com.hola.launcher", "Hola"),
    ("com.smartnews.android", "SmartNews"),
    ("com.topbuzz.videoen", "TopBuzz"),
    ("com.news.break.usa", "News Break"),
    ("com.opera.news", "Opera News"),
    ("com.squidapp.squid", "Squid"),
    ("com.google.android.apps.books", "Google Play Books"),

    # ═══════════════════════════════════════════════════════════════════
    # FOTOĞRAF & VİDEO DÜZENLEME (40 adet)
    # ═══════════════════════════════════════════════════════════════════
    ("com.vsco.cam", "VSCO"),
    ("com.picsart.studio", "PicsArt"),
    ("com.niksoftware.snapseed", "Snapseed"),
    ("com.adobe.lrmobile", "Lightroom"),
    ("com.adobe.psmobile", "Photoshop Express"),
    ("com.canva.editor", "Canva"),
    ("com.camerasideas.instashot", "InShot"),
    ("com.kinemaster.editor.pro", "KineMaster"),
    ("com.capcut.app", "CapCut"),
    ("com.lightricks.videoleap", "Videoleap"),
    ("com.lightricks.facetune.free", "Facetune"),
    ("com.pixlr.express", "Pixlr"),
    ("com.google.android.apps.photos", "Google Photos"),
    ("com.instagram.layout", "Layout"),
    ("com.google.android.apps.youtube.creator", "YouTube Studio"),
    ("video.like", "Likee"),
    ("com.ss.android.ugc.aweme", "TikTok"),
    ("com.vimage.android", "VIMAGE"),
    ("com.photoroom.studio", "PhotoRoom"),
    ("com.remove.bg", "Remove.bg"),
    ("com.retrica", "Retrica"),
    ("com.apperto.funimate", "Funimate"),
    ("com.magisto", "Magisto"),
    ("com.videoshow.editor", "VideoShow"),
    ("com.xvideostudio.videoeditormaker", "Video Editor"),
    ("com.vivavideo.videoeditor", "VivaVideo"),
    ("com.vee.quik", "Quik"),
    ("com.gopro.smarty", "GoPro"),
    ("com.polarr.photoeditor", "Polarr"),
    ("com.airbrush.app", "AirBrush"),
    ("com.beautycam.app", "BeautyCam"),
    ("com.meitu.beautycamera", "MeituPic"),
    ("cn.xender.sn", "Xender"),
    ("com.lomotif.android", "Lomotif"),
    ("com.vinkle.app", "Vinkle"),
    ("com.vita.video.editor", "VITA"),
    ("com.snow.android", "SNOW"),
    ("com.cyberlink.powerdirector.DRA140120_05", "PowerDirector"),
    ("com.adobe.premiererush.samsungcampaign", "Premiere Rush"),
    ("com.splice.splice", "Splice"),

    # ═══════════════════════════════════════════════════════════════════
    # OYUNLAR - AKSİYON & FPS & BATTLE ROYALE (40 adet)
    # ═══════════════════════════════════════════════════════════════════
    ("com.activision.callofduty.shooter", "Call of Duty Mobile"),
    ("com.activision.callofduty.warzone", "Warzone Mobile"),
    ("com.tencent.ig", "PUBG Mobile"),
    ("com.pubg.newstate", "PUBG New State"),
    ("com.tencent.tmgp.pubgmhd", "PUBG Mobile HD"),
    ("com.garena.game.ffsea", "Free Fire"),
    ("com.dts.freefireth", "Free Fire MAX"),
    ("com.epicgames.fortnite", "Fortnite"),
    ("com.supercell.brawlstars", "Brawl Stars"),
    ("com.mobile.legends", "Mobile Legends"),
    ("com.riotgames.league.wildrift", "Wild Rift"),
    ("com.netease.newssao2", "Rules of Survival"),
    ("com.nexternal.knives", "Knives Out"),
    ("com.criticalforceentertainment.criticalops", "Critical Ops"),
    ("com.modernstrike.online", "Modern Strike Online"),
    ("com.gameloft.android.ANMP.GloftMCHM", "Modern Combat 5"),
    ("com.ea.game.apex_legends_mobile_bv_row", "Apex Legends Mobile"),
    ("com.netease.eve.en", "EVE Echoes"),
    ("com.madfingergames.deadtrigger2", "Dead Trigger 2"),
    ("com.madfingergames.shadowgunlegends", "Shadowgun Legends"),
    ("com.wb.goog.mkx", "Mortal Kombat X"),
    ("com.wb.goog.injustice.brawler2017", "Injustice 2"),
    ("com.nekki.shadowfight3", "Shadow Fight 3"),
    ("com.nekki.shadowfight", "Shadow Fight 2"),
    ("com.ea.game.starwarscapital_row", "Star Wars Galaxy"),
    ("com.gtarcade.ioe.global", "Infinity Ops"),
    ("com.gameinsight.gobandits", "Guns of Boom"),
    ("com.nordcurrent.cannonshoot", "Sniper Strike"),
    ("com.paradoxinteractive.stellaris", "Stellaris"),
    ("com.my.world.of.tanks.blitz", "World of Tanks Blitz"),
    ("net.wargaming.wot.blitz", "WoT Blitz"),
    ("com.wargaming.wows.blitz", "World of Warships"),
    ("com.pixonic.wwr", "War Robots"),
    ("com.blankmediagames.werewolfgo", "Werewolf Online"),
    ("com.ngame.allstar.eu", "Standoff 2"),
    ("com.axlebolt.standoff2", "Standoff 2"),
    ("com.gamedevltd.destinywarfare", "Destiny Warfare"),
    ("com.firstbloodio.gangstar", "Gangstar Vegas"),
    ("com.rockstargames.gtasa", "GTA San Andreas"),
    ("com.rockstargames.gtavc", "GTA Vice City"),

    # ═══════════════════════════════════════════════════════════════════
    # OYUNLAR - STRATEJİ & MOBA & KART (40 adet)
    # ═══════════════════════════════════════════════════════════════════
    ("com.supercell.clashofclans", "Clash of Clans"),
    ("com.supercell.clashroyale", "Clash Royale"),
    ("com.supercell.hayday", "Hay Day"),
    ("com.supercell.boom", "Boom Beach"),
    ("com.blizzard.wtcg.hearthstone", "Hearthstone"),
    ("com.riotgames.legendsofruneterra", "Legends of Runeterra"),
    ("com.plarium.raidlegends", "Raid Shadow Legends"),
    ("com.lilithgames.roc.gp", "Rise of Kingdoms"),
    ("com.lilithgame.hgame.gp", "AFK Arena"),
    ("com.kabam.marvelbattle", "Marvel Contest"),
    ("com.scopely.marvel", "Marvel Strike Force"),
    ("com.netease.onmyoji.gl", "Onmyoji"),
    ("com.miHoYo.GenshinImpact", "Genshin Impact"),
    ("com.HoYoverse.hkrpgoversea", "Honkai Star Rail"),
    ("com.miHoYo.bh3global", "Honkai Impact 3rd"),
    ("com.ea.game.pvzfree_row", "Plants vs Zombies"),
    ("com.ea.game.pvz2_row", "PvZ 2"),
    ("com.igg.castleclash_de", "Castle Clash"),
    ("com.igg.android.mobileroyale", "Mobile Royale"),
    ("com.innogames.foe", "Forge of Empires"),
    ("com.goodgamestudios.eotw", "Empire"),
    ("com.funplus.stateofsurvival.gp", "State of Survival"),
    ("com.topwar.gp", "Top War"),
    ("com.jedigames.WWII", "World War II"),
    ("com.puzzlesandconquest.gp", "Puzzles & Conquest"),
    ("com.yotta.lords", "Lords Mobile"),
    ("com.gameofwar.fire.age", "Game of War"),
    ("com.machine.zone", "Machine Zone"),
    ("com.gameloft.android.ANMP.GloftDMHM", "Disney Magic"),
    ("com.nintendo.zara", "Fire Emblem Heroes"),
    ("com.square_enix.android_googleplay.ffbraveexvius", "FFBE"),
    ("com.aniplex.fategrandorder.en", "Fate/Grand Order"),
    ("com.netmarble.sknightsgb", "Seven Knights"),
    ("jp.co.craftegg.band", "BanG Dream!"),
    ("com.klab.lovelive.allstars.global", "Love Live!"),
    ("com.sega.pjsekai", "Project Sekai"),
    ("com.Genshin.GenshinXiao", "Genshin Game"),
    ("com.arrowgames.pvz.heroes", "PvZ Heroes"),
    ("com.ea.gp.apexchampion", "Apex Champion"),
    ("com.tencent.arena.gp", "Arena of Valor"),

    # ═══════════════════════════════════════════════════════════════════
    # OYUNLAR - CASUAL & PUZZLE & ARCADE (40 adet)
    # ═══════════════════════════════════════════════════════════════════
    ("com.kiloo.subwaysurf", "Subway Surfers"),
    ("com.king.candycrushsaga", "Candy Crush Saga"),
    ("com.king.candycrushsodasaga", "Candy Crush Soda"),
    ("com.king.candycrushjellysaga", "Candy Crush Jelly"),
    ("com.imangi.templerun2", "Temple Run 2"),
    ("com.innersloth.spacemafia", "Among Us"),
    ("com.mojang.minecraftpe", "Minecraft"),
    ("com.roblox.client", "Roblox"),
    ("com.playrix.gardenscapes", "Gardenscapes"),
    ("com.playrix.homescapes", "Homescapes"),
    ("com.playrix.township", "Township"),
    ("com.playrix.fishdomdd.gplay", "Fishdom"),
    ("com.outfit7.talkingtom2", "Talking Tom 2"),
    ("com.halfbrick.fruitninjax", "Fruit Ninja"),
    ("com.rovio.angrybirds2.revo", "Angry Birds 2"),
    ("com.rovio.dream", "Angry Birds Dream"),
    ("com.miniclip.eightballpool", "8 Ball Pool"),
    ("com.fingersoft.hillclimb", "Hill Climb Racing"),
    ("com.fingersoft.hcr2", "Hill Climb Racing 2"),
    ("com.zynga.words3", "Words With Friends"),
    ("com.etermax.preguntados.lite", "Trivia Crack"),
    ("com.bitstrip.bitstrips", "Bitmoji"),
    ("com.grailgames.sudoku", "Sudoku"),
    ("com.ketchapp.helix", "Helix Jump"),
    ("io.voodoo.paper2", "Paper.io 2"),
    ("io.voodoo.crowdcity", "Crowd City"),
    ("com.azurgames.stackball", "Stack Ball"),
    ("com.shootbubble.shootbubble", "Bubble Shooter"),
    ("com.matchingham.games.royal.match", "Royal Match"),
    ("com.peak.saloon.match", "Tile Match"),
    ("com.gram.games.mergemagicr", "Merge Magic"),
    ("com.gramgames.mergedragons", "Merge Dragons"),
    ("com.melsoft.woodblock.puzzle", "Wood Block Puzzle"),
    ("com.wordbrain.android", "WordBrain"),
    ("com.peoplefun.wordcross", "Word Cross"),
    ("com.bitmango.go.wordcookies", "Word Cookies"),
    ("com.wordscapes.levels", "Wordscapes"),
    ("com.wordgame.answers.search.puzzle", "Word Search"),
    ("com.peoplefun.wordle", "Wordle"),
    ("games.easybrain.crossword", "Crossword"),

    # ═══════════════════════════════════════════════════════════════════
    # OYUNLAR - SPOR & YARIŞ (40 adet)
    # ═══════════════════════════════════════════════════════════════════
    ("com.ea.gp.fifamobile", "EA FC Mobile"),
    ("com.firsttouchgames.dls7", "Dream League Soccer"),
    ("com.konami.pesam", "eFootball"),
    ("com.ea.games.r3_row", "Real Racing 3"),
    ("com.gameloft.android.ANMP.GloftA9HM", "Asphalt 9"),
    ("com.gameloft.android.ANMP.GloftA8HM", "Asphalt 8"),
    ("com.naturalmotiongames.csr2", "CSR Racing 2"),
    ("com.sega.sonic", "Sonic Dash"),
    ("com.miniclip.golf.battle", "Golf Battle"),
    ("com.ea.games.nfs14_row", "Need for Speed"),
    ("com.ea.game.nfs.mobile", "NFS No Limits"),
    ("com.tencent.tmgp.speedmobile", "QQ Speed"),
    ("com.fingersoft.hillclimb2", "Hill Climb 2"),
    ("com.topgames.basketball", "Basketball Stars"),
    ("com.miniclip.basketballstars", "Basketball Stars"),
    ("com.nba2k.android", "NBA 2K Mobile"),
    ("com.ea.gp.nbamobile", "NBA Mobile"),
    ("com.miniclip.footballstrike", "Football Strike"),
    ("com.nway.powerrangers", "Power Rangers"),
    ("com.badlandspublishing.cue", "Cue"),
    ("com.tacticalgamers.golf", "Golf King"),
    ("com.tennis.clash", "Tennis Clash"),
    ("com.wildlife.games.tennis", "Tennis Game"),
    ("com.matchington.mansion", "Matchington Mansion"),
    ("com.zynga.mlb", "MLB Tap Sports"),
    ("com.naturalmotion.golfchallenge", "Golf Challenge"),
    ("com.gameloft.android.ANMP.GloftS8HM", "Asphalt Street"),
    ("com.topgames.sports", "Top Sports"),
    ("com.miniclip.pool", "Pool"),
    ("com.realcar.parking.driving", "Real Car Parking"),
    ("com.racing.fever", "Racing Fever"),
    ("com.zeptolab.ctr2.f2p.google", "Cut the Rope 2"),
    ("com.ubisoft.hungrysharkworld", "Hungry Shark World"),
    ("com.ubisoft.hungrysharkevolution", "Hungry Shark Evolution"),
    ("com.noodlecake.altosadventure", "Alto's Adventure"),
    ("com.noodlecake.altosodyssey", "Alto's Odyssey"),
    ("com.farseer.golf", "Golf Clash"),
    ("com.playgendary.kickthebuddy", "Kick the Buddy"),
    ("com.carxtech.carxdrift2", "CarX Drift Racing 2"),
    ("com.csr.csrracing2", "CSR 2"),

    # ═══════════════════════════════════════════════════════════════════
    # SAĞLIK & FİTNESS & SPOR (40 adet)
    # ═══════════════════════════════════════════════════════════════════
    ("com.strava", "Strava"),
    ("com.myfitnesspal.android", "MyFitnessPal"),
    ("com.calm.android", "Calm"),
    ("com.headspace.android", "Headspace"),
    ("com.nike.plusgps", "Nike Run Club"),
    ("com.nike.ntc", "Nike Training"),
    ("com.fitbit.FitbitMobile", "Fitbit"),
    ("com.samsung.android.health", "Samsung Health"),
    ("com.google.android.apps.fitness", "Google Fit"),
    ("cc.pacer.androidapp", "Pacer"),
    ("com.noom.coach", "Noom"),
    ("com.flo.health", "Flo"),
    ("com.sweatco.sweatcoin", "Sweatcoin"),
    ("com.runtastic.android", "Adidas Running"),
    ("com.mapmyrun.android2", "MapMyRun"),
    ("com.endomondo.android", "Endomondo"),
    ("com.fitnesskeeper.runkeeper.pro", "Runkeeper"),
    ("com.freeletics.lite", "Freeletics"),
    ("com.sworkit.partner.workouts", "Sworkit"),
    ("homeworkout.homeworkouts.noequipment", "Home Workout"),
    ("com.sixpack.absworkout", "Six Pack"),
    ("com.popularapp.sevenmins", "7 Minute Workout"),
    ("com.workout.timer.tabata", "Tabata Timer"),
    ("com.lezynefit.app", "Lose Weight"),
    ("com.period.tracker.lite", "Period Tracker"),
    ("com.clue.android", "Clue"),
    ("com.pregnancy.tracker", "Pregnancy Tracker"),
    ("com.babycenter.pregnancytracker", "BabyCenter"),
    ("com.meditation.sounds.sleep", "Sleep Sounds"),
    ("co.thefabulous.app", "Fabulous"),
    ("com.habitnow.app", "HabitNow"),
    ("com.loop.loop", "Loop Habit Tracker"),
    ("com.waterlogged.drink", "Water Tracker"),
    ("com.drinkwater.reminder.android", "Drink Water"),
    ("com.yazio.android", "YAZIO"),
    ("com.lifesum.android.lifesum", "Lifesum"),
    ("com.loseit.android", "Lose It!"),
    ("com.cronometer.android", "Cronometer"),
    ("com.app.yoga.daily", "Daily Yoga"),
    ("com.dowdog.yoga", "Down Dog"),

    # ═══════════════════════════════════════════════════════════════════
    # EĞİTİM & ÖĞRENME (40 adet)
    # ═══════════════════════════════════════════════════════════════════
    ("com.duolingo", "Duolingo"),
    ("com.coursera.app", "Coursera"),
    ("com.udemy.android", "Udemy"),
    ("com.linkedin.android.learning", "LinkedIn Learning"),
    ("com.quizlet.quizletandroid", "Quizlet"),
    ("org.khanacademy.android", "Khan Academy"),
    ("com.sololearn", "SoloLearn"),
    ("co.babbel.mobile.android.en", "Babbel"),
    ("com.busuu.android.enc", "Busuu"),
    ("com.skillshare.Skillshare", "Skillshare"),
    ("com.memrise.android.memrisecompanion", "Memrise"),
    ("com.photomath.app", "Photomath"),
    ("com.brainly", "Brainly"),
    ("com.chegg.study", "Chegg Study"),
    ("com.grammarly.android.keyboard", "Grammarly KB"),
    ("com.grammarly", "Grammarly"),
    ("com.google.android.apps.classroom", "Google Classroom"),
    ("com.cambly.mobile", "Cambly"),
    ("com.italki.app", "italki"),
    ("com.rosettastone.mobile.learner", "Rosetta Stone"),
    ("com.drops.android", "Drops"),
    ("com.hellotalk.android", "HelloTalk"),
    ("com.tandem.app", "Tandem"),
    ("com.lingoda.app", "Lingoda"),
    ("com.mindsnacks.tutor", "Tutor"),
    ("com.mimo.application", "Mimo"),
    ("com.grasshopper", "Grasshopper"),
    ("com.enki.insights", "Enki"),
    ("com.programming.hub.python", "Programming Hub"),
    ("com.freecodecamp.android", "freeCodeCamp"),
    ("com.datacamp.android", "DataCamp"),
    ("org.edx.mobile", "edX"),
    ("com.udacity.android", "Udacity"),
    ("com.codecademy.codecademy", "Codecademy"),
    ("com.brilliant.android", "Brilliant"),
    ("com.microsoft.math", "Microsoft Math"),
    ("com.gauthmath.gauthmath", "Gauthmath"),
    ("com.symbolab.android", "Symbolab"),
    ("com.wolfram.android.alpha", "Wolfram Alpha"),
    ("com.mathway", "Mathway"),

    # ═══════════════════════════════════════════════════════════════════
    # ARAÇLAR & VERİMLİLİK (40 adet)
    # ═══════════════════════════════════════════════════════════════════
    ("com.google.android.inputmethod.latin", "Gboard"),
    ("com.touchtype.swiftkey", "SwiftKey"),
    ("com.lastpass.lpandroid", "LastPass"),
    ("com.onepassword.android", "1Password"),
    ("com.bitwarden.ui", "Bitwarden"),
    ("org.torproject.torbrowser", "Tor Browser"),
    ("com.microsoft.office.word", "Microsoft Word"),
    ("com.microsoft.office.excel", "Microsoft Excel"),
    ("com.microsoft.office.powerpoint", "PowerPoint"),
    ("com.microsoft.office.outlook", "Outlook"),
    ("com.microsoft.teams", "Microsoft Teams"),
    ("com.google.android.apps.docs", "Google Docs"),
    ("com.google.android.apps.docs.editors.sheets", "Google Sheets"),
    ("com.google.android.apps.docs.editors.slides", "Google Slides"),
    ("com.google.android.apps.meetings", "Google Meet"),
    ("com.google.android.calendar", "Google Calendar"),
    ("com.notion.id", "Notion"),
    ("com.todoist", "Todoist"),
    ("com.ticktick.task", "TickTick"),
    ("md.obsidian", "Obsidian"),
    ("com.anydo", "Any.do"),
    ("com.evernote", "Evernote"),
    ("com.microsoft.onenote", "OneNote"),
    ("com.automattic.simplenote", "SimpleNote"),
    ("com.google.android.keep", "Google Keep"),
    ("com.slack", "Slack"),
    ("us.zoom.videomeetings", "Zoom"),
    ("com.Slack", "Slack"),
    ("com.discord", "Discord"),
    ("com.trello", "Trello"),
    ("com.asana.app", "Asana"),
    ("com.monday.android", "Monday.com"),
    ("com.zapier.zaps", "Zapier"),
    ("com.ifttt.ifttt", "IFTTT"),
    ("com.dropbox.android", "Dropbox"),
    ("com.google.android.apps.drive", "Google Drive"),
    ("com.microsoft.skydrive", "OneDrive"),
    ("com.box.android", "Box"),
    ("org.mozilla.firefox", "Firefox"),
    ("com.opera.browser", "Opera"),

    # ═══════════════════════════════════════════════════════════════════
    # STREAMING & EĞLENCE & TV (40 adet)
    # ═══════════════════════════════════════════════════════════════════
    ("com.netflix.mediaclient", "Netflix"),
    ("com.disney.disneyplus", "Disney+"),
    ("com.hbo.hbonow", "Max (HBO)"),
    ("com.amazon.avod.thirdpartyclient", "Prime Video"),
    ("com.google.android.youtube", "YouTube"),
    ("com.google.android.youtube.tv", "YouTube TV"),
    ("tv.pluto.android", "Pluto TV"),
    ("com.peacocktv.peacockandroid", "Peacock"),
    ("com.paramount.paramount", "Paramount+"),
    ("com.crunchyroll.crunchyroid", "Crunchyroll"),
    ("com.funimation.funimation_play", "Funimation"),
    ("com.apple.atve", "Apple TV"),
    ("com.curiositystream.tv", "CuriosityStream"),
    ("com.hulu.plus", "Hulu"),
    ("com.sling", "Sling TV"),
    ("com.fubotv.vtv", "fuboTV"),
    ("com.philo.philo", "Philo"),
    ("com.showtime.standalone", "Showtime"),
    ("com.starz.starzplay", "Starz"),
    ("com.mgm.epix", "Epix"),
    ("com.tubitv", "Tubi"),
    ("com.plex.client.android", "Plex"),
    ("com.kodi.android", "Kodi"),
    ("com.vlc.android", "VLC"),
    ("com.mxtech.videoplayer.ad", "MX Player"),
    ("org.videolan.vlc", "VLC"),
    ("com.player.live.tv", "Live TV"),
    ("com.tivimate.companion", "TiViMate"),
    ("ar.tvplayer.tv", "TV Player"),
    ("com.dazn.android", "DAZN"),
    ("com.espn.espnandroid", "ESPN"),
    ("com.foxsports.android", "FOX Sports"),
    ("com.nbcsports.gold", "NBC Sports"),
    ("com.turner.tnt", "TNT"),
    ("com.discovery.discoverygo", "Discovery"),
    ("com.a.e.lifetime", "Lifetime"),
    ("com.hallmark.hallmarkmovies", "Hallmark"),
    ("com.abc.abcplayer", "ABC"),
    ("com.cbs.app", "CBS"),
    ("com.tbs.tbs", "TBS"),
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
        
    model = genai.GenerativeModel('gemini-2.0-flash')
    
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
