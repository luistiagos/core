/**
 * Copyright (C) 2008 Jeremy Thomerson <jeremy@thomersonfamily.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wicketstuff.pageserializer.kryo2.inspecting.analyze.report;

import java.util.ArrayList;
import java.util.List;

import org.wicketstuff.pageserializer.kryo2.inspecting.analyze.ISerializedObjectTree;
import org.wicketstuff.pageserializer.kryo2.inspecting.analyze.ImmutableTree;
import org.wicketstuff.pageserializer.kryo2.inspecting.analyze.ObjectId;

public class Trees
{
	private Trees()
	{
		// no instance
	}

	public static boolean equals(ISerializedObjectTree a, ISerializedObjectTree b)
	{
		if (a == b)
			return true;
		if ((a == null) || (b == null))
			return false;
		if (!a.type().equals(b.type()))
			return false;
		if (!equals(a.label(), b.label()))
			return false;
		if (a.size() != b.size())
			return false;
		if (!equals(a.id(), b.id()))
			return false;
		return equals(a.children(), b.children());
	}

	public static boolean equals(String a, String b)
	{
		if (a == b)
			return true;
		if ((a == null) || (b == null))
			return false;
		return a.equals(b);
	}

	public static boolean equals(ObjectId a, ObjectId b)
	{
		if (a == b)
			return true;
		if ((a == null) || (b == null))
			return false;
		return a.equals(b);
	}
	
	public static boolean equals(List<? extends ISerializedObjectTree> a,
		List<? extends ISerializedObjectTree> b)
	{
		if (a == b)
			return true;
		if ((a == null) || (b == null))
			return false;
		if (a.size() != b.size())
			return false;
		for (int i = 0; i < a.size(); i++)
		{
			if (!equals(a.get(i), b.get(i)))
				return false;
		}
		return true;
	}

	public static Builder build(ObjectId id, Class<?> type, int size)
	{
		return build(id, type, null, size);
	}

	public static Builder build(ObjectId id, Class<?> type, String label, int size)
	{
		return new Builder(id, type, label, size);
	}

	public static class Builder
	{
		private final ObjectId id;
		private final Class<?> type;
		private final String label;
		private final int size;
		private final Builder parent;
		private final List<Builder> children = new ArrayList<Trees.Builder>();

		private Builder(ObjectId id, Class<?> type, String label, int size)
		{
			this(null, id, type, label, size);
		}

		private Builder(Builder parent, ObjectId id, Class<?> type, String label, int size)
		{
			this.id = id;
			this.type = type;
			this.label = label;
			this.size = size;
			this.parent = parent;
		}

		public Builder withChild(ObjectId id, Class<?> type, int size)
		{
			return withChild(id, type, null, size);
		}

		public Builder withChild(ObjectId id, Class<?> type, String label, int size)
		{
			Builder child = new Builder(this, id, type, label, size);
			children.add(child);
			return child;
		}

		private Builder withCopy(Builder s)
		{
			Builder child = new Builder(this, s.id, s.type, s.label, s.size);
			for (Builder c : s.children)
			{
				child.withCopy(c);
			}
			children.add(child);
			return child;
		}

		public Builder parent()
		{
			return parent;
		}

		public Builder root()
		{
			if (parent != null)
				return parent.root();
			return this;
		}

		public ISerializedObjectTree asTree()
		{
			return root().buildTree();
		}

		private ISerializedObjectTree buildTree()
		{
			List<ISerializedObjectTree> items = new ArrayList<ISerializedObjectTree>();
			for (Builder b : children)
			{
				items.add(b.buildTree());
			}
			return new ImmutableTree(id, type, label, size, items);
		}

		public Builder multiply(int count)
		{
			for (int i = 0; i < (count - 1); i++)
			{
				parent.withCopy(this);
			}
			return this;
		}

	}
}