/*
 * Copyright 2016 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.ehviewer.client.parser;

import static com.hippo.ehviewer.client.data.ListUrlBuilder.MODE_NORMAL;

import com.hippo.ehviewer.GetText;
import com.hippo.ehviewer.R;
import com.hippo.ehviewer.client.data.GalleryInfo;
import com.hippo.ehviewer.client.exception.EhException;
import com.hippo.ehviewer.client.exception.ParseException;
import com.hippo.util.ExceptionUtils;
import com.hippo.util.JsoupUtils;
import com.hippo.lib.yorozuya.AssertUtils;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FavoritesParser {

    public static class Result {
        public String[] catArray; // Size 10
        public int[] countArray; // Size 10
        public int pages;
        public int nextPage;
        public String resultCount;
        public String firstHref;
        public String prevHref;
        public String nextHref;
        public String lastHref;
        public String favOrder;
        public List<GalleryInfo> galleryInfoList;
    }

    public static Result parse(String body) throws Exception {
        if (body.contains("This page requires you to log on.</p>")) {
            throw new EhException(GetText.getString(R.string.need_sign_in));
        }
        Result re = new Result();

        String[] catArray = new String[10];
        int[] countArray = new int[10];
        Document d;
        try {
            d = Jsoup.parse(body);
            Element ido = JsoupUtils.getElementByClass(d, "ido");
            //noinspection ConstantConditions
            Elements fps = ido.getElementsByClass("fp");
            // Last one is "fp fps"
            AssertUtils.assertEquals(11, fps.size());

            for (int i = 0; i < 10; i++) {
                Element fp = fps.get(i);
                countArray[i] = ParserUtils.parseInt(fp.child(0).text(), 0);
                catArray[i] = ParserUtils.trim(fp.child(2).text());
            }
            Elements orders = JsoupUtils.getElementsByClass(d,"searchnav");
            if (null!=orders&&!orders.isEmpty()){
                Element order = orders.first();
                if (null!=order){
                    Elements selects = order.getElementsByTag("select");
                    if (!selects.isEmpty()){
                        Element sel = selects.get(0);
                        Elements result = sel.getElementsByAttribute("selected");
                        re.favOrder = result.attr("value");
                    }
                }
            }
        } catch (Throwable e) {
            ExceptionUtils.throwIfFatal(e);
            e.printStackTrace();
            throw new ParseException("Parse favorites error", body);
        }

        GalleryListParser.Result result = GalleryListParser.parse(d,body, MODE_NORMAL);


        re.catArray = catArray;
        re.countArray = countArray;
        re.pages = result.pages;
        re.nextPage = result.nextPage;
        re.resultCount = result.resultCount;
        re.firstHref = result.firstHref;
        re.nextHref = result.nextHref;
        re.prevHref = result.prevHref;
        re.lastHref = result.lastHref;
        re.galleryInfoList = result.galleryInfoList;

        return re;
    }
}
