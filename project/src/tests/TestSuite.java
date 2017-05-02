package tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({FileHelperTests.class, LoaderTest.class, LocationTest.class,
    ManagerTests.class, PalletTest.class, PickerTest.class, ReplenisherTest.class,
    SequencerTest.class, TruckTest.class, WarehouseTests.class, ZoneTest.class})
public class TestSuite {

}
