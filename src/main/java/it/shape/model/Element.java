package it.shape.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;


@Entity
@Table(name = "ELEMENT", uniqueConstraints = @UniqueConstraint(columnNames = "NAME"))
public class Element implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "table_generator")
    @TableGenerator(name = "table_generator", table = "SEQUENCE_GENERATOR", pkColumnName = "NAME", valueColumnName = "NEXT_VAL", allocationSize = 100)
    @Column(name = "ID")
    protected Long id;

    @Column(nullable = false)
    protected String name;

    @Column
    protected String dirty;

    @OneToMany
    @JoinColumn(name = "CNE_ID")
    protected Set<Element> cascadeNoneElements = new LinkedHashSet<>();

    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "CPE_ID")
    protected Set<Element> cascadePersistElements = new LinkedHashSet<>();

    @OneToMany(cascade = CascadeType.MERGE)
    @JoinColumn(name = "CME_ID")
    protected Set<Element> cascadeMergeElements = new LinkedHashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "CAE_ID")
    protected Set<Element> cascadeAllElements = new LinkedHashSet<>();

    @PrePersist
    public void prePersist()
    {
        System.out.println("prePersist: " + this);
    }

    @PostPersist
    public void postPersist()
    {
        System.out.println("postPersist: " + this);
    }

    @PreUpdate
    public void preUpdate()
    {
        System.out.println("preUpdate: " + this);
    }

    @PostUpdate
    public void postUpdate()
    {
        System.out.println("postUpdate: " + this);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(this == obj)
        {
            return true;
        }

        if(obj == null || getClass() != obj.getClass())
        {
            return false;
        }

        return Objects.equals(name, ((Element) obj).name);
    }

    @Override
    public String toString()
    {
        return super.toString() + " - " + name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDirty()
    {
        return dirty;
    }

    public void setDirty(String dirty)
    {
        this.dirty = dirty;
    }

    public Set<Element> getCascadeNoneElements()
    {
        return cascadeNoneElements;
    }

    public void setCascadeNoneElements(Set<Element> cascadeNoneElements)
    {
        this.cascadeNoneElements = cascadeNoneElements;
    }

    public Set<Element> getCascadePersistElements()
    {
        return cascadePersistElements;
    }

    public void setCascadePersistElements(Set<Element> cascadePersistElements)
    {
        this.cascadePersistElements = cascadePersistElements;
    }

    public Set<Element> getCascadeMergeElements()
    {
        return cascadeMergeElements;
    }

    public void setCascadeMergeElements(Set<Element> cascadeMergeElements)
    {
        this.cascadeMergeElements = cascadeMergeElements;
    }

    public Set<Element> getCascadeAllElements()
    {
        return cascadeAllElements;
    }

    public void setCascadeAllElements(Set<Element> cascadeAllElements)
    {
        this.cascadeAllElements = cascadeAllElements;
    }
}
