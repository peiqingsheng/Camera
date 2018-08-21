/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.view;

import android.support.v4.util.ArrayMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A collection class that automatically groups {@link Size}s by their {@link AspectRatio}s.
 */
class SizeMap {

    private final ArrayMap<AspectRatio, SortedSet<Size>> mRatios = new ArrayMap<>();
    private List<Size> mSizes = new ArrayList<>();

    /**
     * Add a new {@link Size} to this collection.
     *
     * @param size The size to add.
     * @return {@code true} if it is added, {@code false} if it already exists and is not added.
     */
    public boolean add(Size size) {
        for (AspectRatio ratio : mRatios.keySet()) {
            if (ratio.matches(size)) {
                final SortedSet<Size> sizes = mRatios.get(ratio);
                if (sizes.contains(size)) {
                    return false;
                } else {
                    sizes.add(size);
                    mSizes.add(size);
                    return true;
                }
            }
        }
        // None of the existing ratio matches the provided size; add a new key
        SortedSet<Size> sizes = new TreeSet<>();
        sizes.add(size);
        mSizes.add(size);
        mRatios.put(AspectRatio.of(size.getWidth(), size.getHeight()), sizes);
        return true;
    }

    /**
     * Removes the specified aspect ratio and all sizes associated with it.
     *
     * @param ratio The aspect ratio to be removed.
     */
    public void remove(AspectRatio ratio) {
        mRatios.remove(ratio);
    }

    Set<AspectRatio> ratios() {
        return mRatios.keySet();
    }

    SortedSet<Size> sizes(AspectRatio ratio) {
        SortedSet<Size> sizes = mRatios.get(ratio);
        if (sizes == null) {
            return mRatios.valueAt(mRatios.size() - 1);
        }
        return sizes;
    }

    void clear() {
        mRatios.clear();
        mSizes.clear();
    }

    boolean isEmpty() {
        return mRatios.isEmpty();
    }


    /**
     * 获取合适的size
     *
     * @param height 高度
     * @return 相对于竖屏来说 最接近这个height高度的size
     */
    public Size getProperSize(int height) {
        //主width降序，副heigth升序
        Collections.sort(mSizes, new Comparator<Size>() {
            @Override
            public int compare(Size lhs, Size rhs) {
                return lhs.getWidth() < rhs.getWidth() ? 1 : (lhs.getWidth() == rhs.getWidth() ?
                        (lhs.getHeight() < rhs.getHeight() ? -1 : (lhs.getHeight() == rhs.getHeight() ? 0 : 1)) : -1);
            }
        });
        Size result = mSizes.get(0);
        for (Size size : mSizes) {
            if ((size.getWidth() > height ? size.getWidth() - height : height - size.getWidth()) < (result.getWidth() > height ? result.getWidth() - height : height - result.getWidth())) {
                result = size;
            }
        }
        //这个比例的只有这一个适合的尺寸
        for (AspectRatio ratio : mRatios.keySet()) {
            if (ratio.matches(result)) {
                mRatios.get(ratio).clear();
                mRatios.get(ratio).add(result);
                return result;
            }
        }
        SortedSet<Size> sizes = new TreeSet<>();
        sizes.add(result);
        mRatios.put(AspectRatio.of(result.getWidth(), result.getHeight()), sizes);
        return result;
    }

}
