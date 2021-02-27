package me.sirrus86.s86powers.gui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.sirrus86.s86powers.localization.LocaleString;

public abstract class GUIAbstractList<T extends Comparable<T>> extends GUIAbstract {

	private final Collection<T> baseList;
	List<T> list = new ArrayList<>();
	
	List<GUIAbstractList<T>> sourceList = new ArrayList<>();
	
	final static ItemStack PAGE1 = new ItemStack(Material.PAPER),
			PAGE2 = new ItemStack(Material.PAPER);
	
	int page = 1;
	
	public GUIAbstractList(int page, Collection<T> list) {
		super(6, LocaleString.LIST.toString());
		this.page = page;
		this.baseList = Collections.unmodifiableCollection(list);
		this.list = Lists.newArrayList(baseList);
		Collections.sort(this.list);
	}
	
	@SafeVarargs
	static <X extends GUIAbstractList<Y>, Y extends Comparable<Y>> List<X> createLists(Class<X> clazz, Collection<Y> list, Collection<Y>... filters) {
		int size = list.size();
		if (filters != null) {
			for (int i = 0; i < filters.length; i ++) {
				size -= filters[i].size();
			}
		}
		List<X> lists = new ArrayList<>();
		int j = 1;
		for (int i = size; i > 0; i -= 45) {
			try {
				X newList = clazz.getConstructor(int.class, Collection.class).newInstance(j, list);
				newList.filterList(filters);
				lists.add(newList);
				j ++;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
		return lists;
	}
	
	@SafeVarargs
	final void filterList(Collection<T>... filters) {
		list = Lists.newArrayList(baseList);
		for (int i = 0; i < filters.length; i ++) {
			list.removeAll(filters[i]);
		}
		Collections.sort(list);
	}
	
	@SuppressWarnings("unchecked")
	<X extends GUIAbstractList<T>> void setSourceList(List<X> newSourceList) {
		this.sourceList = (List<GUIAbstractList<T>>) newSourceList;
	}

}
