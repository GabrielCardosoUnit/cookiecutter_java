#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
class AppTest
{
    /**
     * Rigourous Test :-)
     */
    @Test
    void testApp()
    {
        assertTrue( true );
    }

    /**
     * Garante que a execucao de App.main() realmente exercita o codigo
     * (necessario para a checagem de cobertura minima do JaCoCo).
     */
    @Test
    void mainPrintsHelloWorld()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut( new PrintStream( out ) );
        try
        {
            App.main( new String[] {} );
        }
        finally
        {
            System.setOut( original );
        }
        assertEquals( "Hello World!" + System.lineSeparator(), out.toString() );
    }
}
