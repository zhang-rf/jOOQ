/**
 * Copyright (c) 2009-2016, Data Geekery GmbH (http://www.datageekery.com)
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * ASL 2.0 and offer limited warranties, support, maintenance, and commercial
 * database integrations.
 *
 * For more information, please visit: http://www.jooq.org/licenses
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package org.jooq.impl;

import static java.util.Arrays.asList;
import static org.jooq.Clause.DROP_TABLE;
import static org.jooq.Clause.DROP_TABLE_TABLE;
// ...
// ...
// ...
import static org.jooq.SQLDialect.DERBY;
import static org.jooq.SQLDialect.FIREBIRD;
// ...
// ...

import org.jooq.Clause;
import org.jooq.Configuration;
import org.jooq.Context;
import org.jooq.DropTableStep;
import org.jooq.Table;


/**
 * @author Lukas Eder
 */
class DropTableImpl extends AbstractQuery implements

    // Cascading interface implementations for DROP TABLE behaviour
    DropTableStep {

    /**
     * Generated UID
     */
    private static final long     serialVersionUID = 8904572826501186329L;
    private static final Clause[] CLAUSES          = { DROP_TABLE };

    private final Table<?>        table;
    private final boolean         ifExists;
    private boolean               cascade;

    DropTableImpl(Configuration configuration, Table<?> table) {
        this(configuration, table, false);
    }

    DropTableImpl(Configuration configuration, Table<?> table, boolean ifExists) {
        super(configuration);

        this.table = table;
        this.ifExists = ifExists;
    }

    // ------------------------------------------------------------------------
    // XXX: DSL API
    // ------------------------------------------------------------------------

    @Override
    public final DropTableImpl cascade() {
        cascade = true;
        return this;
    }

    @Override
    public final DropTableImpl restrict() {
        cascade = false;
        return this;
    }

    // ------------------------------------------------------------------------
    // XXX: QueryPart API
    // ------------------------------------------------------------------------

    private final boolean supportsIfExists(Context<?> ctx) {
        return !asList(DERBY, FIREBIRD).contains(ctx.family());
    }

    @Override
    public final void accept(Context<?> ctx) {
        if (ifExists && !supportsIfExists(ctx)) {
            Utils.executeImmediateBegin(ctx, DDLStatementType.DROP_TABLE);
            accept0(ctx);
            Utils.executeImmediateEnd(ctx, DDLStatementType.DROP_TABLE);
        }
        else {
            accept0(ctx);
        }
    }

    private void accept0(Context<?> ctx) {
        ctx.start(DROP_TABLE_TABLE)
           .keyword("drop table").sql(' ');

        if (ifExists && supportsIfExists(ctx))
            ctx.keyword("if exists").sql(' ');

        ctx.visit(table);

        if (cascade) {
            ctx.sql(' ').keyword("cascade");
        }

        ctx.end(DROP_TABLE_TABLE);
    }


    @Override
    public final Clause[] clauses(Context<?> ctx) {
        return CLAUSES;
    }
}
