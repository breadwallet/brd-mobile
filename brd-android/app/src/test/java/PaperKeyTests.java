/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 11/3/17.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
import android.util.Log;

import com.breadwallet.tools.util.Bip39Reader;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PaperKeyTests {

    private static final String TAG = PaperKeyTests.class.getName();
    public static final String PAPER_KEY_JAP = "こせき　ぎじにってい　けっこん　せつぞく　うんどう　ふこう　にっすう　こせい　きさま　なまみ　たきび　はかい";//japanese
    public static final String PAPER_KEY_ENG = "stick sword keen   afraid smile sting   huge relax nominee   arena area gift ";//english
    public static final String PAPER_KEY_FRE = "vocation triage capsule marchand onduler tibia illicite entier fureur minorer amateur lubie";//french
    public static final String PAPER_KEY_SPA = "zorro turismo mezcla nicho morir chico blanco pájaro alba esencia roer repetir";//spanish
    public static final String PAPER_KEY_CHI = "怨 贪 旁 扎 吹 音 决 廷 十 助 畜 怒";//chinese


//    @Test
//    public void testWordsValid() {
//
//        List<String> list = getAllWords();
//        assertThat(list.size(), is(10240));
//
//        assertThat(isValid(PAPER_KEY_JAP, list), is(true));
//        assertThat(isValid(PAPER_KEY_ENG, list), is(true));
//        assertThat(isValid(PAPER_KEY_FRE, list), is(true));
//        assertThat(isValid(PAPER_KEY_SPA, list), is(true));
//        assertThat(isValid(PAPER_KEY_CHI, list), is(true));
//    }

    @Test
    public void testPaperKeyValidation() {
        List<String> list = getAllWords();
        assertThat(list.size(), is(10240));
    }

    private List<String> getAllWords() {
        List<String> result = new ArrayList<>();
        List<String> names = new ArrayList<>();
        names.add("en-BIP39Words.txt");
        names.add("es-BIP39Words.txt");
        names.add("fr-BIP39Words.txt");
        names.add("ja-BIP39Words.txt");
        names.add("zh-BIP39Words.txt");

        for (String fileName : names) {
            InputStream in = null;
            try {
                in = getClass().getResourceAsStream(fileName);
                String str = IOUtils.toString(in);
                String lines[] = str.split("\\r?\\n");
                result.addAll(Arrays.asList(lines));
            } catch (IOException e) {
                Log.e(TAG, "getAllWords: " + fileName + ", ", e);
            } finally {
                if (in != null) try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        List<String> cleanList = new ArrayList<>();
        for (String s : result) {
            String cleanWord = Bip39Reader.cleanWord(s);
            cleanList.add(cleanWord);
        }
        assertThat(cleanList.size(), is(10240));
        return cleanList;
    }

//    private boolean isValid(String phrase, List<String> words) {
//
//        return WalletsMaster.getInstance(null).validateRecoveryPhrase((String[]) words.toArray(), phrase);
//    }

}
