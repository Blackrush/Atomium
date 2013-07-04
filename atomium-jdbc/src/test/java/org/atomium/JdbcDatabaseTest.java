package org.atomium;

import com.google.common.base.Suppliers;
import org.atomium.annotations.Column;
import org.atomium.annotations.PrimaryKey;
import org.atomium.dialects.SqlDialects;
import org.atomium.metadata.SimpleMetadataRegistry;
import org.junit.*;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Blackrush
 */
public class JdbcDatabaseTest {
    private static Connection connection;

    @BeforeClass
    public static void setUpTests() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
    }

    @AfterClass
    public static void tearDownTests() throws Exception {
        connection.close();
    }

    private JdbcDatabase db;

    @Before
    public void setUp() throws Exception {
        db = JdbcDatabase.of(
                Suppliers.ofInstance(connection),
                SqlDialects.forDatabaseMetaData(connection.getMetaData()),
                new SimpleMetadataRegistry()
        );
        db.getRegistry().register(MyEntity.class);
    }

    @After
    public void tearDown() throws Exception {
        // don't 'db.close();' because we already do it in '@AfterClass void tearDownTests()'
    }

    @Test
    public void testRefPkey() throws Exception {
        Ref<MyEntity> ref = db.ref(MyEntity.class, 1);
        assertThat(ref.getColumn().isPrimaryKey(), is(true));
        assertThat(ref.getIdentifier(), is((Object) 1));
    }

    @Test
    public void testRefColumn() throws Exception {
        Ref<MyEntity> ref = db.ref(MyEntity.class, "attr", "wrong attr lel");
        assertThat(ref.getColumn().getName(), is("attr"));
        assertThat(ref.getIdentifier(), is((Object) "wrong attr lel"));
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class MyEntity {
        @Column
        @PrimaryKey(autogenerated = true)
        private int id;

        @Column
        private String attr;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getAttr() {
            return attr;
        }

        public void setAttr(String attr) {
            this.attr = attr;
        }
    }
}
