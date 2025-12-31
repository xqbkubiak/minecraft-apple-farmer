export interface ModVersion {
    version: string
    date: string
    gameVersion: string[]
    changes: string[]
    downloadUrl: string
    sourceUrl?: string
    size: string
}

export interface Mod {
    id: number
    title: string
    description: string
    image: string
    category: string
    downloads: number
    version: string
    game: string[]
    featured: boolean
    longDescription: string
    features: string[]
    videoUrl?: string
    gallery: string[]
    history: ModVersion[]
}

export const modsData: Mod[] = [
    {
        id: 1,
        title: "BK-Apple",
        description: "Najpotężniejszy bot do automatycznego farmienia jabłek! Zbieraj, jedz, naprawiaj i uzupełniaj zapasy automatycznie.",
        longDescription: "BK-Apple to profesjonalny i całkowicie przebudowany mod Fabric wspierający wiele wersji gry (1.20.1, 1.20.4, 1.20.6 oraz całą serię 1.21.x). Mod pozwala na pełną automatyzację farmy jabłek w tle, w tym inteligentne uzupełnianie liści i żelaza ze skrzyń, automatyczne wyrzucanie itemów (Dumping) oraz zaawansowany system naprawy narzędzi. Posiada nowoczesne GUI, system konfiguracji w folderze config/BK-Mods oraz pełne wsparcie dla języka polskiego.",
        image: "/apple-bot-farming.png",
        category: "Automation",
        downloads: 0,
        version: "3.0",
        game: ["1.20.1", "1.20.4", "1.20.6", "1.21.x"],
        featured: true,
        features: [
            "Wsparcie dla Minecraft 1.20.x oraz 1.21.x",
            "Smart Restock - Automatyczne dobieranie liści i żelaza ze skrzyni",
            "Działa w tle (nawet przy zminimalizowanej grze)",
            "Auto-Repair (Komenda lub Crafting nożyc)",
            "Auto-Eat (Inteligentne zarządzanie głodem)",
            "Auto Storage & Dumping (Odkładanie zbiorów)",
            "Nowoczesne GUI z pełną konfiguracją",
            "Wsparcie PL/EN"
        ],
        videoUrl: "https://www.youtube.com/watch?v=gv2Ei0MvS6s",
        gallery: [
            "/minecraft-optifine-shaders-graphics.jpg",
            "/minecraft-minimap-world-map-navigation.jpg"
        ],
        history: [
            {
                version: "3.0",
                date: "2024-12-31",
                gameVersion: ["1.20.1", "1.20.4", "1.20.6", "1.21.x"],
                size: "82 KB",
                downloadUrl: "/downloads/bk-apple-3.0.zip",
                sourceUrl: "https://github.com/xqbkubiak/minecraft-apple-farmer",
                changes: [
                    "Nowa nazwa bota: BK-Apple",
                    "System Smart Restock (automatyczne dopełnianie zapasów)",
                    "Wsparcie dla Minecraft 1.20.6 (Data Component API)",
                    "Pełna kompatybilność z wersjami 1.20.x oraz 1.21.x",
                    "Nowy system konfiguracji w folderze BK-Mods",
                    "Optymalizacja GUI i stabilności"
                ]
            },
            {
                version: "2.7",
                date: "2024-12-30",
                gameVersion: ["1.21.4"],
                size: "76 KB",
                downloadUrl: "/downloads/applebot-2.7.jar",
                sourceUrl: "https://github.com/xqbkubiak/minecraft-apple-farmer",
                changes: ["Wsparcie dla 1.21.4 (Fabric)", "Nowy system Pickup Cooldown", "Ulepszone GUI"]
            }
        ]
    }
]
