package com.mvukosav.scoreagentsvas.service

interface AgentsNotificationService{
    fun showNotification(title: String, content: String, notifId: Int)
    fun showNotification2(title: String, content: String, notifId: Int)
}