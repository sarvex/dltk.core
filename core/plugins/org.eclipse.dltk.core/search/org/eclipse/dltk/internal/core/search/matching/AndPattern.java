/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.dltk.compiler.util.SimpleSet;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchParticipant;
import org.eclipse.dltk.core.search.SearchPattern;
import org.eclipse.dltk.core.search.index.EntryResult;
import org.eclipse.dltk.core.search.index.Index;
import org.eclipse.dltk.internal.core.search.IndexQueryRequestor;

/**
 * Query the index multiple times and do an 'and' on the results.
 */
public abstract class AndPattern extends DLTKSearchPattern {

	public AndPattern(int patternKind, int matchRule,
			IDLTKLanguageToolkit toolkit) {
		super(patternKind, matchRule, toolkit);
	}

	public void findIndexMatches(Index index, IndexQueryRequestor requestor,
			SearchParticipant participant, IDLTKSearchScope scope,
			IProgressMonitor progressMonitor) throws IOException {
		if (progressMonitor != null && progressMonitor.isCanceled())
			throw new OperationCanceledException();

		this.resetQuery();
		SimpleSet intersectedNames = null;
		try {
			index.startQuery();
			do {
				SearchPattern pattern = ((InternalSearchPattern) this)
						.currentPattern();
				EntryResult[] entries = ((InternalSearchPattern) pattern)
						.queryIn(index);
				if (entries == null)
					return;

				SearchPattern decodedResult = pattern.getBlankPattern();
				SimpleSet newIntersectedNames = new SimpleSet(3);
				for (int i = 0, l = entries.length; i < l; i++) {
					if (progressMonitor != null && progressMonitor.isCanceled())
						throw new OperationCanceledException();

					EntryResult entry = entries[i];
					decodedResult.decodeIndexKey(entry.getWord());
					if (pattern.matchesDecodedKey(decodedResult)) {
						String[] names = entry.getDocumentNames(index);
						if (intersectedNames != null) {
							for (int j = 0, n = names.length; j < n; j++)
								if (intersectedNames.includes(names[j]))
									newIntersectedNames.add(names[j]);
						} else {
							for (int j = 0, n = names.length; j < n; j++)
								newIntersectedNames.add(names[j]);
						}
					}
				}

				if (newIntersectedNames.elementSize == 0)
					return;
				intersectedNames = newIntersectedNames;
			} while (this.hasNextQuery());
		} finally {
			index.stopQuery();
		}

		final String containerPath = index.getContainerPath();
		final char separator = index.separator;
		Object[] names = intersectedNames.values;
		for (int i = 0, l = names.length; i < l; i++)
			if (names[i] != null)
				((InternalSearchPattern) this).acceptMatch((String) names[i],
						containerPath, separator, null/* no pattern */,
						requestor, participant, scope); // AndPatterns cannot
														// provide
		// the decoded result
	}

	/**
	 * Returns whether another query must be done.
	 */
	public abstract boolean hasNextQuery();

	/**
	 * Resets the query and prepares this pattern to be queried.
	 */
	public abstract void resetQuery();
}
