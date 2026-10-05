package com.beemdevelopment.aegis;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.beemdevelopment.aegis.otp.TotpInfo;
import com.beemdevelopment.aegis.ui.MainActivity;
import com.beemdevelopment.aegis.ui.views.EntryAdapter;
import com.beemdevelopment.aegis.vault.VaultRepository;
import com.beemdevelopment.aegis.vault.VaultRepositoryException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import dagger.hilt.android.testing.HiltAndroidTest;

@RunWith(AndroidJUnit4.class)
@HiltAndroidTest
public class SearchTest extends AegisTest {
    @Before
    public void before() throws VaultRepositoryException {
        VaultRepository vault = initEmptyPlainVault();
        vault.addEntry(generateEntry(TotpInfo.class, "Alice", "Google"));
        vault.addEntry(generateEntry(TotpInfo.class, "Bob", "GitHub"));
        _vaultManager.save();
    }

    @Test
    public void testClearSubmittedSearch() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            assertShownEntries(2);
            onView(withId(androidx.appcompat.R.id.search_button)).perform(click());
            onView(withId(androidx.appcompat.R.id.search_src_text))
                    .perform(typeText("Google"), pressImeActionButton());
            // Submitting collapses the search field without clearing its filter.
            assertShownEntries(1);

            onView(withId(androidx.appcompat.R.id.search_button)).perform(click());
            onView(withId(androidx.appcompat.R.id.search_src_text)).perform(clearText());

            assertShownEntries(2);
            // Live filtering continues to work after clearing the submitted search.
            onView(withId(androidx.appcompat.R.id.search_src_text)).perform(typeText("GitHub"));
            assertShownEntries(1);
            onView(withId(androidx.appcompat.R.id.search_src_text)).perform(clearText());
            assertShownEntries(2);
        }
    }

    @Test
    public void testClearUnsubmittedSearch() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(androidx.appcompat.R.id.search_button)).perform(click());
            onView(withId(androidx.appcompat.R.id.search_src_text)).perform(typeText("Google"));
            assertShownEntries(1);

            onView(withId(androidx.appcompat.R.id.search_src_text)).perform(clearText());

            assertShownEntries(2);
        }
    }

    private void assertShownEntries(int expectedCount) {
        onView(withId(R.id.rvKeyProfiles)).check((view, error) -> {
            if (error != null) {
                throw error;
            }
            EntryAdapter adapter = (EntryAdapter) ((RecyclerView) view).getAdapter();
            assertEquals(expectedCount, adapter.getShownEntriesCount());
        });
    }
}
