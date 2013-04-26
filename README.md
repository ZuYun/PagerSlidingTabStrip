Android ViewPagerIndicator
==========================

Interactive paging indicator widget, compatible with the `ViewPager` from the 
Android Support Library. Based on Google Play Store 4.0.25


Usage
=====

*For a working implementation of this project see the `sample/` folder.*

  1. Include the PagerSlidingTabStrip widget in your view. This should usually be placed
     adjacent to the `ViewPager` it represents.

        <com.astuetz.viewpager.extensions.PagerSlidingTabStrip
            android:id="@+id/tabs"
            android:layout_width="match_parent"
            android:layout_height="48dip" />

  2. In your `onCreate` method (or `onCreateView` for a fragment), bind the
     widget to the `ViewPager`.

         // Set the pager with an adapter
         ViewPager pager = (ViewPager) findViewById(R.id.pager);
         pager.setAdapter(new TestAdapter(getSupportFragmentManager()));

         // Bind the widget to the adapter
         PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
         tabs.setViewPager(pager);

  3. *(Optional)* If you use an `OnPageChangeListener` with your view pager
     you should set it in the widget rather than on the pager directly.

         // continued from above
         tabs.setOnPageChangeListener(mPageChangeListener);


Developed By
============

 * Andreas Stuetz - <andreas.stuetz@gmail.com>


Credits
-------

 * [Kirill Grouchnikov][1] - Author of [an explanation post][2], on Google+


License
=======

    Copyright 2013 Andreas Stuetz

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.





 [1]: https://plus.google.com/108761828584265913206/posts
 [2]: https://plus.google.com/108761828584265913206/posts/Cwk7joBV3AC