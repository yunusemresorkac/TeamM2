package com.app.gamenews.di

import com.app.gamenews.viewmodel.ChatViewModel
import com.app.gamenews.viewmodel.CommentViewModel
import com.app.gamenews.viewmodel.EpicViewModel
import com.app.gamenews.viewmodel.FollowersViewModel
import com.app.gamenews.viewmodel.FollowingViewModel
import com.app.gamenews.viewmodel.JoinersViewModel
import com.app.gamenews.viewmodel.MyPostViewModel
import com.app.gamenews.viewmodel.PostViewModel
import com.app.gamenews.viewmodel.RoomChatViewModel
import com.app.gamenews.viewmodel.RoomViewModel
import com.app.gamenews.viewmodel.SteamViewModel
import com.app.gamenews.viewmodel.TimeViewModel
import com.app.gamenews.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    viewModel{
        SteamViewModel(get())
    }

    viewModel{
        EpicViewModel(get())
    }

    viewModel{
        CommentViewModel(get())
    }

    viewModel{
        UserViewModel(get())
    }

    viewModel{
        ChatViewModel(get())
    }

    viewModel{
        PostViewModel(get())
    }
    viewModel{
        MyPostViewModel(get())
    }

    viewModel{
        FollowersViewModel(get())
    }

    viewModel{
        FollowingViewModel(get())
    }

    viewModel{
        TimeViewModel(get())
    }

    viewModel{
        RoomViewModel(get())
    }

    viewModel{
        JoinersViewModel(get())
    }

    viewModel{
        RoomChatViewModel(get())
    }


}