package learning.android.appwidgetsample

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.DateFormat
import java.util.*

const val sharedPrefFile = "learning.android.appwidgetsample"
const val COUNT_KEY = "count"

/**
 * Implementation of App Widget functionality.
 */
class AppWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Get the intent
    val idArray = arrayOf(appWidgetId)
    val intentUpdate = Intent(context, AppWidget::class.java).apply{
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray.toIntArray())
    }
    // Prepare pending intent
    val pendingUpdate = PendingIntent.getBroadcast(
            context, appWidgetId, intentUpdate, PendingIntent.FLAG_UPDATE_CURRENT)

    // Get updates count
    val prefs = context.getSharedPreferences(sharedPrefFile, 0)
    var count = prefs.getInt(COUNT_KEY + appWidgetId, 0)
    ++count

    // Get current date/time
    val dateString = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.app_widget)
    views.setTextViewText(R.id.appwidget_id, appWidgetId.toString())
    views.setTextViewText(R.id.appwidget_update,
            context.resources.getString(R.string.date_count_format, count, dateString))

    // Set onClickListener
    views.setOnClickPendingIntent(R.id.button_update, pendingUpdate)

    // Put count back into prefs
    val prefsEditor = prefs.edit()
    prefsEditor.putInt(COUNT_KEY + appWidgetId, count)
    prefsEditor.apply()

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}