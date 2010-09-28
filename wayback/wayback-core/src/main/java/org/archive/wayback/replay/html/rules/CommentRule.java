/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.replay.html.rules;

import java.io.IOException;
import java.io.OutputStream;

import org.archive.wayback.replay.html.ReplayParseEventDelegator;
import org.archive.wayback.replay.html.ReplayParseEventDelegatorVisitor;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.util.htmllex.ParseContext;
import org.archive.wayback.util.htmllex.handlers.CloseTagHandler;
import org.archive.wayback.util.htmllex.handlers.OpenTagHandler;
import org.htmlparser.Node;
import org.htmlparser.nodes.TagNode;

public class CommentRule implements ReplayParseEventDelegatorVisitor, 
	OpenTagHandler, CloseTagHandler {

	private final static byte[] startComment = "<!--".getBytes();
	private final static byte[] endComment = "-->".getBytes();
	
	public void emit(ReplayParseContext context, Node node) throws IOException {
		OutputStream os = context.getOutputStream();
		if(os != null) {
			os.write(startComment);
			os.write(node.toHtml(true).getBytes());
			os.write(endComment);
		}
	}

	public void visit(ReplayParseEventDelegator rules) {
		rules.getPreModifyDelegator().addOpenTagHandler(this);
		rules.getPreModifyDelegator().addCloseTagHandler(this, "A");
	}

	public void handleOpenTagNode(ParseContext context, TagNode node) throws IOException {
		emit((ReplayParseContext)context,node);
	}

	public void handleCloseTagNode(ParseContext context, TagNode node)
			throws IOException {
		emit((ReplayParseContext)context,node);
	}
}
