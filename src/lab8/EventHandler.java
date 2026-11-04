/*  
202419094
Nguyen Chinh Thai
*/
package lab8;

@FunctionalInterface
public interface EventHandler<T> {
    void handle(Object sender, T event);
}
