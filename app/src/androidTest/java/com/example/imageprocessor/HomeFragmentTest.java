package com.example.imageprocessor;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.imageprocessor.ui.home.HomeFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class HomeFragmentTest {

    @Test
    public void testNavigationToCameraScreen() {
        NavController mockNavController = mock(NavController.class);
        FragmentScenario<HomeFragment> homeFragmentScenario =
                FragmentScenario.launchInContainer(HomeFragment.class);
//        homeFragmentScenario.moveToState(Lifecycle.State.STARTED);
        homeFragmentScenario.onFragment(fragment ->
                Navigation.setViewNavController(fragment.requireView(), mockNavController));

        onView(ViewMatchers.withId(R.id.cameraCardView))
                .perform(ViewActions.click());
        verify(mockNavController).navigate(R.id.action_nav_home_to_nav_camera);
//        homeFragmentScenario.moveToState(Lifecycle.State.DESTROYED);
    }
}
