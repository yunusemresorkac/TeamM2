package com.app.gamenews.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gamenews.R
import com.app.gamenews.adapter.GameNewsAdapter
import com.app.gamenews.databinding.FragmentNewsBinding
import com.app.gamenews.model.GameNews
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL

class NewsFragment : Fragment() {

    private lateinit var binding : FragmentNewsBinding
    private lateinit var gameNewsAdapter: GameNewsAdapter
    private lateinit var newsList : ArrayList<GameNews>
    private var scrolling = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewsBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuBar = activity?.findViewById<LinearLayout>(R.id.menuLay)

        menuBar?.let { showOrHideMenuWithAnimation(it) }

        initRecycler()

        FetchXmlData().execute("https://www.oyungunlugu.com/category/haber/feed/")



    }

    private fun showMenuWithFadeIn(menuBar: LinearLayout) {
        val fadeIn = ObjectAnimator.ofFloat(menuBar, "alpha", 0f, 1f)
        fadeIn.duration = 500 // Animasyon süresi, istediğin gibi ayarlayabilirsin

        fadeIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                menuBar.visibility = View.VISIBLE
            }
        })

        fadeIn.start()
    }

    private fun hideMenuWithFadeOut(menuBar: LinearLayout) {
        val fadeOut = ObjectAnimator.ofFloat(menuBar, "alpha", 1f, 0f)
        fadeOut.duration = 500 // Animasyon süresi, istediğin gibi ayarlayabilirsin

        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                menuBar.visibility = View.GONE
            }
        })

        fadeOut.start()
    }

    private fun showOrHideMenuWithAnimation(menuBar: LinearLayout) {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                scrolling = newState == RecyclerView.SCROLL_STATE_DRAGGING
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0 && menuBar.visibility == View.VISIBLE && !scrolling) {
                    hideMenuWithFadeOut(menuBar)
                } else if (dy < 0 && menuBar.visibility != View.VISIBLE && !scrolling) {
                    showMenuWithFadeIn(menuBar)
                }
            }
        })
    }


    private fun initRecycler(){
        newsList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        gameNewsAdapter = GameNewsAdapter(newsList,requireContext())
        binding.recyclerView.adapter = gameNewsAdapter
    }

    inner class FetchXmlData : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg urls: String): String {
            val urlString = urls[0]
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            val response = StringBuilder()

            try {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                connection.disconnect()
            }
            return response.toString()
        }

        override fun onPostExecute(result: String) {
            val gameNewsList = parseXml(result)
            gameNewsAdapter.setGameNewsList(gameNewsList)
        }
    }

    private fun parseXml(xmlString: String): ArrayList<GameNews> {
        val gameNewsList = ArrayList<GameNews>()

        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlString))

        var eventType = parser.eventType
        var currentGameNews: GameNews? = null
        var inDescription = false
        var descriptionContent = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "item" -> currentGameNews = GameNews()
                        "title" -> currentGameNews?.title = parser.nextText()
                        "link" -> currentGameNews?.link = parser.nextText()
                        "description" -> {
                            inDescription = true
                            descriptionContent = ""
                        }
                        "pubDate" -> currentGameNews?.pubdate = parser.nextText()
                        "guid" -> currentGameNews?.guid = parser.nextText()
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inDescription) {
                        descriptionContent += parser.text
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "item") {
                        currentGameNews?.let { gameNewsList.add(it) }
                    } else if (parser.name == "description") {
                        inDescription = false
                        val cleanedDescription = cleanDescription(descriptionContent)
                        currentGameNews?.description = cleanedDescription
                    }
                }
            }
            eventType = parser.next()
        }

        return gameNewsList
    }

    private fun cleanDescription(description: String): String {
        val pattern = Regex("<(.*?)>")
        var cleanedDescription = description.replace(pattern, "")

        // Remove the last sentence ("first appeared on oyungunlugu") from description
        val lastSentenceIndex = cleanedDescription.lastIndexOf('f')
        if (lastSentenceIndex != -1) {
            cleanedDescription = cleanedDescription.substring(0, lastSentenceIndex + 1)
        }

        return cleanedDescription
    }

}