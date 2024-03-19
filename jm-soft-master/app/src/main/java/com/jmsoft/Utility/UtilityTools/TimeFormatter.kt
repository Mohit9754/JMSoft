package com.jmsoft.basic.UtilityTools

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Throws

object TimeFormatter {
    @SuppressLint("SimpleDateFormat")
    @Throws(ParseException::class)
    fun timeAgo(date: String?, context: Context?, Pattern: String?): String {
        val dateFormat1 = SimpleDateFormat(Pattern, Locale.getDefault())
        dateFormat1.timeZone = TimeZone.getTimeZone("UTC")
        val time_ago = dateFormat1.parse(date.toString())
        val currenttime = System.currentTimeMillis()
        val diff = currenttime - time_ago!!.time
        val diffSeconds = diff / 1000
        val diffMinutes = diff / (60 * 1000) % 60
        val diffHours = diff / (60 * 60 * 1000) % 24
        val diffDays = diff / (24 * 60 * 60 * 1000)
        var time: String? = null
        time = if (diffDays > 0) {
            if (diffDays == 1L) {
                "$diffDays day ago "
            } else if (diffDays <= 7) {
                "$diffDays days ago "
            } else {
                val dateFormat = SimpleDateFormat("MMMM dd,yyyy")
                dateFormat.format(time_ago)
            }
        } else {
            if (diffHours > 0) {
                if (diffHours == 1L) {
                    "$diffHours hr ago"
                } else {
                    "$diffHours hrs ago"
                }
            } else {
                if (diffMinutes > 0) {
                    if (diffMinutes == 1L) {
                        "$diffMinutes min ago"
                    } else {
                        "$diffMinutes mins ago"
                    }
                } else {
                    if (diffSeconds <= 0) {
                        "Now"
                    } else {
                        "$diffSeconds sec ago"
                    }
                }
            }
        }
        return time!!
    }

    @Throws(ParseException::class)
    fun TimeAgo(date: String?, context: Context?, Pattern: String?): String {
        val dateFormat1 = SimpleDateFormat(Pattern, Locale.getDefault())
        dateFormat1.timeZone = TimeZone.getTimeZone("UTC")
        val time_ago = dateFormat1.parse(date.toString())
        val currenttime = System.currentTimeMillis()
        val diff = currenttime - time_ago!!.time
        val diffSeconds = diff / 1000
        val diffMinutes = diff / (60 * 1000) % 60
        val diffHours = diff / (60 * 60 * 1000) % 24
        val diffDays = diff / (24 * 60 * 60 * 1000)
        var time: String? = null
        time = if (diffDays > 0) {
            diffDays.toString() + "D"
        } else {
            if (diffHours > 0) {
                diffHours.toString() + "H"
            } else {
                if (diffMinutes > 0) {
                    diffMinutes.toString() + "M"
                } else {
                    if (diffSeconds <= 0) {
                        "Now"
                    } else {
                        diffSeconds.toString() + "S"
                    }
                }
            }
        }
        return time
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(ParseException::class)
    fun timeAgo(date: String?, Pattern: String?): String {
        val format = SimpleDateFormat(Pattern, Locale.ENGLISH)
        var time_ago: Date? = null
        try {
            time_ago = format.parse(date.toString())
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val currenttime = System.currentTimeMillis()
        val diff = currenttime - time_ago!!.time
        val diffSeconds = diff / 1000
        val diffMinutes = diff / (60 * 1000) % 60
        val diffHours = diff / (60 * 60 * 1000) % 24
        val diffDays = diff / (24 * 60 * 60 * 1000)
        val diffWeeks = diff / (7 * 24 * 60 * 60 * 1000) % 7
        var time: String?
        time = if (diffWeeks > 0) {
            if (diffWeeks == 1L) {
                "$diffWeeks week ago "
            } else {
                "$diffWeeks weeks ago "
            }
        } else if (diffDays > 0) {
            if (diffDays == 1L) {
                "$diffDays day ago "
            } else if (diffDays <= 7) {
                "$diffDays days ago "
            } else {
                val dateFormat = SimpleDateFormat("MMMM dd,yyyy")
                dateFormat.format(time_ago)
            }
        } else {
            if (diffHours > 0) {
                if (diffHours == 1L) {
                    "$diffHours hour ago"
                } else {
                    "$diffHours hours ago"
                }
            } else {
                if (diffMinutes > 0) {
                    if (diffMinutes == 1L) {
                        "$diffMinutes min ago"
                    } else {
                        "$diffMinutes mins ago"
                    }
                } else {
                    if (diffSeconds <= 0) {
                        "Now"
                    } else {
                        "$diffSeconds sec ago"
                    }
                }
            }
        }
        return time!!
    }

    @SuppressLint("SimpleDateFormat")
    fun timeAgo(time_ago: Date): String {
        val currenttime = System.currentTimeMillis()
        val diff = currenttime - time_ago.time
        val diffSeconds = diff / 1000
        val diffMinutes = diff / (60 * 1000) % 60
        val diffHours = diff / (60 * 60 * 1000) % 24
        val diffDays = diff / (24 * 60 * 60 * 1000)
        var time: String? = null
        time = if (diffDays > 0) {
            if (diffDays == 1L) {
                "$diffDays d ago "
            } else if (diffDays <= 7) {
                "$diffDays d ago "
            } else {
                val dateFormat = SimpleDateFormat("MMMM dd,yyyy")
                dateFormat.format(time_ago)
            }
        } else {
            if (diffHours > 0) {
                if (diffHours == 1L) {
                    "$diffHours h ago"
                } else {
                    "$diffHours h ago"
                }
            } else {
                if (diffMinutes > 0) {
                    if (diffMinutes == 1L) {
                        "$diffMinutes m ago"
                    } else {
                        "$diffMinutes m ago"
                    }
                } else {
                    if (diffSeconds <= 0) {
                        "Now"
                    } else {
                        "$diffSeconds s ago"
                    }
                }
            }
        }
        return time!!
    }

    @SuppressLint("SimpleDateFormat")
    fun changeDateFormat(time: String?, inputPattern: String = Constants.dateFormat, outputPattern: String = "dd/MM/yyyy"): String? {

        val inputFormat = SimpleDateFormat(inputPattern)
        val outputFormat = SimpleDateFormat(outputPattern)
        var date: Date? = null
        var str: String? = null
        try {
            date = inputFormat.parse(time.toString())
            str = outputFormat.format(date!!)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return str
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(ParseException::class)
    fun macthTime(time: String?): Boolean {
        val inputPattern = "yyyy-MM-dd HH:mm:ss"
        val inputFormat = SimpleDateFormat(inputPattern)
        val date = inputFormat.parse(time.toString())
        return !Date().after(date)
    }

    @Throws(ParseException::class)
    fun getFormattedDate(date: String?, context: Context?, Pattern: String?): String {
        val dateFormat1 = SimpleDateFormat(Pattern, Locale.getDefault())
        dateFormat1.timeZone = TimeZone.getTimeZone("UTC")
        val date1 = dateFormat1.parse(date.toString())
        val smsTime = Calendar.getInstance()
        if (date1 != null) {
            smsTime.timeInMillis = date1.time
        }
        val now = Calendar.getInstance()
        val timeFormatString = "h:mm aa"
        val dateTimeFormatString = "dd,MMM yyyy"
        val HOURS = (60 * 60 * 60).toLong()
        return if (now[Calendar.DATE] == smsTime[Calendar.DATE]) {
            //return "Today"+ " " + DateFormat.format(timeFormatString, smsTime);
            "Today"
        } else if (now[Calendar.DATE] - smsTime[Calendar.DATE] == 1) {
            //return "Yesterday" + " " + DateFormat.format(timeFormatString, smsTime);
            "Yesterday"
        } else if (now[Calendar.YEAR] == smsTime[Calendar.YEAR]) {
            DateFormat.format(dateTimeFormatString, smsTime).toString()
        } else {
            DateFormat.format("MMM dd yyyy", smsTime).toString()
        }
    }

    @Throws(ParseException::class)
    fun getDateTime(date: String?, Pattern: String?, For: String): String {
        val dateFormat1 = SimpleDateFormat(Pattern, Locale.getDefault())
        // dateFormat1.setTimeZone(TimeZone.getTimeZone("UTC"));
        val date1 = dateFormat1.parse(date.toString())
        val smsTime = Calendar.getInstance()
        if (date1 != null) {
            smsTime.timeInMillis = date1.time
        }
        return if (For == "Date") {
            DateFormat.format("dd MMM yyyy", smsTime).toString()
        } else {
            DateFormat.format("h:mm aa", smsTime).toString()
        }
    }
}