package com.andrews.mirai.data.source.sakura

import com.andrews.mirai.data.source.madara.MadaraSource
import com.andrews.mirai.data.source.madara.MadaraSourceConfig

class SakuraSource : MadaraSource(

    config = MadaraSourceConfig(

        id = "sakura",

        name = "Sakura Mangás",

        baseUrl = "https://sakuramangas.org"

    )

)