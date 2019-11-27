# RecyclerViewAdapter

The extended version of `RecyclerView.Adapter`. For example, you can easy put items of different classes to your `RecyclerView` and use different `ViewHolder` classes for them.
Also there is some support for touch gestures using `ItemTouchHelper`.

## Usage

Execute this command under your project root:

    git submodule add https://github.com/KivApple/RecyclerViewAdapter.git recyclerviewadapter

Add these lines to your Android project files:

`/settings.gradle`

    include ':app', ..., ':recyclerviewadapter'

`/build.gradle`

    buildscript {
        ext.kotlin_version = '1.3.50'
        ext.appCompatVersion = '1.0.2'
        ext.recyclerViewVersion = '1.0.0'
        ...
    }

`/app/build.gradle`

    dependencies {
        ...
        implementation project(':recyclerviewadapter')
        ...
    }
