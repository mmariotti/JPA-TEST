package it.shape.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import it.shape.model.Element;


/**
 * The Class CascadeTest.
 *
 * @author Michele Mariotti
 */
@RunWith(Parameterized.class)
public class CascadeTest
{
    protected static EntityManagerFactory emf;

    protected static final Object[] SUMMARY_TITLE = { "PARENT_STATE", "CHILD_STATE", "METHOD", "CASCADE", "EXPECTED", "SUCCESS", "FINAL_RESULT" };

    protected static final List<Object[]> SUMMARY = new ArrayList<>();

    @Parameter(0)
    public String parentState;

    @Parameter(1)
    public String childState;

    @Parameter(2)
    public String method;

    @Parameter(3)
    public String cascade;

    @Parameter(4)
    public boolean expected;

    @Parameters(name = "parentState={0}, childState={1}, method={2}, cascade={3}")
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][] {
            { "transient", "transient", "persist", "none", false },
            { "transient", "transient", "persist", "persist", true },
            { "transient", "transient", "persist", "merge", false },
            { "transient", "transient", "persist", "all", true },
            { "transient", "transient", "merge", "none", false },
            { "transient", "transient", "merge", "persist", false },
            { "transient", "transient", "merge", "merge", true },
            { "transient", "transient", "merge", "all", true },
            { "transient", "detached", "persist", "none", true },
            { "transient", "detached", "persist", "persist", false },
            { "transient", "detached", "persist", "merge", true },
            { "transient", "detached", "persist", "all", false },
            { "transient", "detached", "merge", "none", true },
            { "transient", "detached", "merge", "persist", true },
            { "transient", "detached", "merge", "merge", true },
            { "transient", "detached", "merge", "all", true },
            { "detached", "transient", "persist", "none", false },
            { "detached", "transient", "persist", "persist", false },
            { "detached", "transient", "persist", "merge", false },
            { "detached", "transient", "persist", "all", false },
            { "detached", "transient", "merge", "none", false },
            { "detached", "transient", "merge", "persist", true },
            { "detached", "transient", "merge", "merge", true },
            { "detached", "transient", "merge", "all", true },
            { "detached", "detached", "persist", "none", false },
            { "detached", "detached", "persist", "persist", false },
            { "detached", "detached", "persist", "merge", false },
            { "detached", "detached", "persist", "all", false },
            { "detached", "detached", "merge", "none", true },
            { "detached", "detached", "merge", "persist", true },
            { "detached", "detached", "merge", "merge", true },
            { "detached", "detached", "merge", "all", true }
        });
    }

    @Test
    public void test() throws Throwable
    {
        System.out.println("----------------------------------------");
        System.out.println("---- parentState: " + parentState +
            ", childState: " + childState +
            ", method: " + method +
            ", cascade: " + cascade);
        System.out.println("----------------------------------------");

        try
        {
            transact(em ->
            {
                Element parent = createElement(em, parentState);
                System.out.println("---- parent: " + parent);

                Element child = createElement(em, childState);
                System.out.println("---- child: " + child);

                add(parent, child, cascade);

                System.out.println("---- saving...");

                save(em, parent, method);
            });

            System.out.println("---- result: success");
            SUMMARY.add(new Object[] { parentState, childState, method, cascade, expected, true, expected ? "OK" : "ERROR" });

            if(!expected)
            {
                throw new RuntimeException();
            }
        }
        catch(Throwable e)
        {
            Throwable rootCause = ExceptionUtils.getRootCause(e);

            System.out.println("---- result: failed - " + rootCause.getMessage());
            SUMMARY.add(new Object[] { parentState, childState, method, cascade, expected, false, !expected ? "OK" : "ERROR" });

            if(expected)
            {
                throw rootCause;
            }
        }
        finally
        {
            System.out.println("----------------------------------------");
            System.out.println();
            System.out.println();
        }
    }

    public Element createElement(EntityManager em, String state)
    {
        Element e1 = new Element();
        e1.setName("element " + System.nanoTime());

        if("transient".equals(state))
        {
            return e1;
        }
        else if("detached".equals(state))
        {
            transact(em2 ->
            {
                em2.persist(e1);
                em2.flush();
                em2.detach(e1);
                emf.getCache().evictAll();
            });

            em.detach(e1);

            return e1;
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    public void add(Element parent, Element child, String cascade)
    {
        if("none".equals(cascade))
        {
            parent.getCascadeNoneElements().add(child);
            parent.setDirty("dirty " + System.nanoTime());
        }
        else if("persist".equals(cascade))
        {
            parent.getCascadePersistElements().add(child);
            parent.setDirty("dirty " + System.nanoTime());
        }
        else if("merge".equals(cascade))
        {
            parent.getCascadeMergeElements().add(child);
            parent.setDirty("dirty " + System.nanoTime());
        }
        else if("all".equals(cascade))
        {
            parent.getCascadeAllElements().add(child);
            parent.setDirty("dirty " + System.nanoTime());
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    public Element save(EntityManager em, Element element, String action)
    {
        element.setDirty("dirty " + System.nanoTime());

        if("persist".equals(action))
        {
            em.persist(element);
            return element;
        }
        else if("merge".equals(action))
        {
            return em.merge(element);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    public void transact(Consumer<EntityManager> consumer)
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();

            consumer.accept(em);

            em.getTransaction().commit();
        }
        catch(Exception e)
        {
            em.getTransaction().rollback();

            throw e;
        }
        finally
        {
            em.close();
        }
    }

    @BeforeClass
    public static void beforeClass()
    {
        emf = Persistence.createEntityManagerFactory("test");
    }

    @AfterClass
    public static void afterClass()
    {
        if(emf != null && emf.isOpen())
        {
            emf.close();
        }

        Stream.concat(Stream.<Object[]> of(SUMMARY_TITLE), SUMMARY.stream())
            .map(x -> Stream.of(x)
                .map(y -> StringUtils.rightPad(String.valueOf(y), 14))
                .collect(Collectors.joining("\t")))
            .forEach(System.out::println);
    }
}
