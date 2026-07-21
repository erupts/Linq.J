document.addEventListener('DOMContentLoaded', () => {

    // ========== i18n ==========
    const translations = {
        zh: {
            'nav.features': '特性',
            'nav.syntax': '语法',
            'nav.examples': '示例',
            'nav.architecture': '架构',
            'nav.scenarios': '场景',
            'nav.start': '快速开始',
            'nav.roadmap': '路线图',
            'hero.badge': 'v0.1.X 已发布 · 查看 GitHub →',
            'hero.title': '不写 for 循环，<br>用&nbsp;<span class="hero-sql-word">SQL</span>&nbsp;思维<br>查询内存数据',
            'hero.desc': 'Linq.J 让 Java 集合查询变得像写 SQL 一样直观。JOIN、WHERE、GROUP BY、ORDER BY，链式一行搞定，代码即文档。',
            'hero.btn-primary': '快速开始',
            'hero.btn-secondary': '查看示例',
            'hero.pill1': '零外部依赖',
            'hero.pill4': 'MIT 开源',
            'stats.label1': '外部依赖',
            'stats.label2': '体积大小',
            'stats.label3': '最低版本',
            'stats.label4': '开源协议',
            'features.tag': '核心特性',
            'features.title': '为什么选择 Linq.J？',
            'features.desc': '轻量、强大、优雅 — 用最少的代码完成最复杂的数据查询',
            'features.card1.title': '零依赖 · 极致轻量',
            'features.card1.desc': '运行时无任何第三方依赖，仅 50KB 大小。引入即用，不会给项目带来任何负担。',
            'features.card2.title': '类 SQL 链式语法',
            'features.card2.desc': 'Select、Join、Where、Group By、Order By、Limit… 用你熟悉的 SQL 思维操作内存数据。',
            'features.card3.title': '类型安全',
            'features.card3.desc': '基于 Lambda 方法引用的列操作，编译期检查字段名，IDE 自动补全，告别魔法字符串。',
            'features.card4.title': '多数据源支持',
            'features.card4.desc': 'List、Array、SQL 结果集、CSV/JSON/XML、Stream… 任何内存中的集合都能查询。',
            'features.card5.title': '可插拔查询引擎',
            'features.card5.desc': '抽象 Engine 接口，默认 EruptEngine 内置 Hash Join 等高效策略，可自定义扩展。',
            'features.card6.title': '丰富的聚合函数',
            'features.card6.desc': 'COUNT、SUM、AVG、MAX、MIN、COUNT DISTINCT… 完整的聚合能力，配合 Group By 使用。',
            'syntax.tag': '操作语法',
            'syntax.title': '完整的 SQL 操作对应',
            'syntax.desc': 'Linq.J 提供了与 SQL 一一对应的操作方法，链式调用一气呵成',
            'syntax.join.title': '关联查询',
            'syntax.where.title': '条件过滤',
            'syntax.select.title': '投影与聚合',
            'syntax.write.title': '结果写出',
            'examples.tag': '代码示例',
            'examples.title': '直观感受 Linq.J 的优雅',
            'examples.desc': '像写 SQL 一样操作 Java 对象，代码即文档',
            'examples.tab.basic': '基础查询',
            'examples.tab.join': '多表关联',
            'examples.tab.where': '条件过滤',
            'examples.tab.group': '分组聚合',
            'examples.tab.write': '结果写出',
            'examples.basic.title': '简洁强大的基础查询',
            'examples.basic.desc': '从简单的排序过滤到复合操作，一行代码搞定。支持原始类型和自定义对象。',
            'examples.basic.li1': '支持原始类型直接查询',
            'examples.basic.li2': '链式调用自然流畅',
            'examples.basic.li3': '结果自动映射到目标类型',
            'examples.join.title': '强大的多表关联',
            'examples.join.desc': '支持四种标准 JOIN 操作，使用 Hash 策略实现高效关联，语法与 SQL 完全对应。',
            'examples.join.li1': 'LEFT / RIGHT / INNER / FULL JOIN',
            'examples.join.li2': 'Hash Join 高效引擎',
            'examples.join.li3': 'Lambda 类型安全关联键',
            'examples.where.title': '灵活的条件过滤',
            'examples.where.desc': '从简单的等值比较到复杂的自定义条件，WHERE 子句覆盖你的所有需求。',
            'examples.where.li1': 'eq / between / in / like / isNull',
            'examples.where.li2': '单字段 Lambda 条件',
            'examples.where.li3': '多字段自定义组合条件',
            'examples.group.title': '分组与聚合',
            'examples.group.desc': 'Group By + 聚合函数 + Having，完整的分组聚合能力，生成统计报表轻而易举。',
            'examples.group.li1': 'GROUP BY 分组',
            'examples.group.li2': 'HAVING 过滤',
            'examples.group.li3': '多聚合函数组合',
            'examples.write.title': '灵活的结果写出',
            'examples.write.desc': '查询结果可以映射为 Java 对象、List、Map 或单个值，满足不同场景需求。',
            'examples.write.li1': 'toList() → List<T>',
            'examples.write.li2': 'one() → T',
            'examples.write.li3': 'toMaps() → List<Map>',
            'examples.write.li4': 'toMap() → Map',
            'arch.tag': '技术架构',
            'arch.title': '清晰的分层设计',
            'arch.desc': '查询构建与执行引擎解耦，接口隔离，灵活可扩展',
            'arch.layer1': '链式 API 层',
            'arch.layer2': '查询模型层',
            'arch.layer3': '执行引擎层',
            'arch.layer4': 'Lambda 解析层',
            'scenarios.tag': '应用场景',
            'scenarios.title': '在哪里使用 Linq.J？',
            'scenarios.desc': '分布式微服务、数据处理、报表生成… 无处不在',
            'scenarios.card1.title': 'RPC 结果关联',
            'scenarios.card1.desc': 'Feign / Dubbo / gRPC 等微服务调用返回的多个结果集，通过 Linq.J 在内存中进行 JOIN 关联，替代多次数据库查询。',
            'scenarios.card2.title': '异构数据计算',
            'scenarios.card2.desc': 'Redis、MongoDB、MySQL 等不同数据源的结果，统一加载到内存后，用 Linq.J 进行跨源查询与聚合。',
            'scenarios.card3.title': 'SQL 结果再加工',
            'scenarios.card3.desc': '数据库查询结果需要进一步排序、过滤、聚合？Linq.J 让代码替代复杂的二次 SQL，逻辑清晰可维护。',
            'scenarios.card4.title': '内存分页与排序',
            'scenarios.card4.desc': '多结果合并后的排序聚合与内存分页，告别手写 for 循环和 if 分支，代码量减少 80%。',
            'scenarios.card5.title': '对象转换映射',
            'scenarios.card5.desc': '语义化地完成对象转换与映射，代码即文档，团队协作更清晰敏捷。',
            'scenarios.card6.title': '跨数据源联邦查询',
            'scenarios.card6.desc': '不同系统、不同存储的数据，通过 Linq.J 在应用层实现联邦访问，无需数据迁移。',
            'start.tag': '快速开始',
            'start.title': '三步上手 Linq.J',
            'start.desc': '零配置，即装即用',
            'start.step1': '添加 Maven 依赖',
            'start.step2': '确保字段有 Getter',
            'start.step3': '开始查询',
            'start.copy': '复制',
            'start.copied': '已复制!',
            'comparison.tag': '对比',
            'comparison.title': '传统写法 vs Linq.J',
            'comparison.before': '传统 Java 写法',
            'comparison.after': 'Linq.J 写法',
            'roadmap.tag': '路线图',
            'roadmap.title': '持续进化中',
            'roadmap.desc': 'Linq.J 正在不断完善，更多强大特性即将到来',
            'roadmap.done': '已完成',
            'roadmap.planned': '计划中',
            'roadmap.item1.title': 'HAVING 支持',
            'roadmap.item1.desc': '分组聚合后的条件过滤',
            'roadmap.item2.title': '分组列格式化',
            'roadmap.item2.desc': '支持 group by date(created_at) 等表达式',
            'roadmap.item3.title': '集合操作',
            'roadmap.item3.desc': 'UNION ALL、UNION、INTERSECT、EXCEPT、UNION BY NAME',
            'roadmap.item4.title': '窗口函数',
            'roadmap.item4.desc': 'ROW_NUMBER、RANK、DENSE_RANK 等分析函数',
            'footer.desc': '基于内存的 Java 对象查询语言<br>灵感源自 C# LINQ',
            'footer.res': '资源',
            'footer.related': '相关',
            'footer.csharp': 'C# LINQ 参考',
            'arch.item.facade': 'Linq (门面)',
            'arch.item.state': 'Dql (查询状态)',
            'arch.item.abstract': 'Engine (抽象)',
            'arch.item.default': 'EruptEngine (默认)',
            'code.basic.cmt1': '// 字符串排序过滤',
            'code.basic.cmt2': '// 结果: [C, B, B]',
            'code.basic.cmt3': '// 数字排序',
            'code.basic.cmt4': '// 结果: [1, 2, 3, 5, 6, 7]',
            'code.join.cmt1': '// Left Join + 多表 Select',
            'code.join.cmt2': '// Inner Join + Where + Distinct',
            'code.where.cmt1': '// 精确匹配',
            'code.where.cmt2': '// 范围查询',
            'code.where.cmt3': '// 自定义单字段条件',
            'code.where.cmt4': '// 多字段组合条件',
            'code.write.cmt1': '// 写出为对象列表',
            'code.write.cmt2': '// 写出单个对象',
            'code.write.cmt3': '// 写出为 Map 列表',
            'code.write.cmt4': '// 写出单个 Map',
            'code.compare.cmt1': '// 还需要排序... 更多代码...',
            'code.compare.cmt2': '// 清晰 · 简洁 · 高效',
            'code.start.cmt1': '// Lombok 注解',
            'page.title': 'Linq.J — Java 内存对象查询语言',
        },
        en: {
            'nav.features': 'Features',
            'nav.syntax': 'Syntax',
            'nav.examples': 'Examples',
            'nav.architecture': 'Architecture',
            'nav.scenarios': 'Use Cases',
            'nav.start': 'Quick Start',
            'nav.roadmap': 'Roadmap',
            'hero.badge': 'v0.1.X Released · View GitHub →',
            'hero.title': 'No more for loops,<br>Query in-memory data<br>with&nbsp;<span class="hero-sql-word">SQL</span>&nbsp;thinking',
            'hero.desc': 'Linq.J makes Java collection queries as intuitive as writing SQL. JOIN, WHERE, GROUP BY, ORDER BY — all in one fluent chain. Code is documentation.',
            'hero.btn-primary': 'Quick Start',
            'hero.btn-secondary': 'View Examples',
            'hero.pill1': 'Zero Dependencies',
            'hero.pill4': 'MIT License',
            'stats.label1': 'Dependencies',
            'stats.label2': 'Package Size',
            'stats.label3': 'Min Version',
            'stats.label4': 'License',
            'features.tag': 'Core Features',
            'features.title': 'Why Choose Linq.J?',
            'features.desc': 'Lightweight, powerful, and elegant — tackle the most complex queries with minimal code',
            'features.card1.title': 'Zero Deps · Ultra Lightweight',
            'features.card1.desc': 'No third-party runtime dependencies, only 50KB. Drop it in and go — zero project overhead.',
            'features.card2.title': 'SQL-like Fluent API',
            'features.card2.desc': 'Select, Join, Where, Group By, Order By, Limit… operate on in-memory data using familiar SQL thinking.',
            'features.card3.title': 'Type Safe',
            'features.card3.desc': 'Column operations via Lambda method references, field names checked at compile time, IDE auto-complete — no magic strings.',
            'features.card4.title': 'Multi-Source Support',
            'features.card4.desc': 'List, Array, SQL result sets, CSV/JSON/XML, Stream… any in-memory collection is queryable.',
            'features.card5.title': 'Pluggable Engine',
            'features.card5.desc': 'Abstract Engine interface; the default EruptEngine includes Hash Join and other efficient strategies, fully customizable.',
            'features.card6.title': 'Rich Aggregations',
            'features.card6.desc': 'COUNT, SUM, AVG, MAX, MIN, COUNT DISTINCT… full aggregation capability, works seamlessly with Group By.',
            'syntax.tag': 'Syntax',
            'syntax.title': 'Complete SQL Correspondence',
            'syntax.desc': 'Linq.J provides one-to-one SQL operation equivalents, all chainable in a single expression',
            'syntax.join.title': 'Join Queries',
            'syntax.where.title': 'Filtering',
            'syntax.select.title': 'Projection & Aggregation',
            'syntax.write.title': 'Output',
            'examples.tag': 'Code Examples',
            'examples.title': 'Experience the Elegance of Linq.J',
            'examples.desc': 'Operate on Java objects like writing SQL — code is documentation',
            'examples.tab.basic': 'Basic Query',
            'examples.tab.join': 'Join',
            'examples.tab.where': 'Filtering',
            'examples.tab.group': 'Grouping',
            'examples.tab.write': 'Output',
            'examples.basic.title': 'Concise and Powerful Basic Queries',
            'examples.basic.desc': 'From simple sorting and filtering to composite operations, done in one line. Supports primitives and custom objects.',
            'examples.basic.li1': 'Primitive type queries supported',
            'examples.basic.li2': 'Natural fluent chaining',
            'examples.basic.li3': 'Results auto-mapped to target types',
            'examples.join.title': 'Powerful Multi-Table Joins',
            'examples.join.desc': 'Supports all four standard JOIN operations using Hash strategy for efficient joins, syntax mirrors SQL exactly.',
            'examples.join.li1': 'LEFT / RIGHT / INNER / FULL JOIN',
            'examples.join.li2': 'Hash Join efficient engine',
            'examples.join.li3': 'Lambda type-safe join keys',
            'examples.where.title': 'Flexible Filtering',
            'examples.where.desc': 'From simple equality checks to complex custom conditions, the WHERE clause covers all your needs.',
            'examples.where.li1': 'eq / between / in / like / isNull',
            'examples.where.li2': 'Single-field Lambda conditions',
            'examples.where.li3': 'Multi-field custom combined conditions',
            'examples.group.title': 'Grouping & Aggregation',
            'examples.group.desc': 'Group By + aggregation functions + Having — full grouping capability, generating statistical reports is effortless.',
            'examples.group.li1': 'GROUP BY grouping',
            'examples.group.li2': 'HAVING filtering',
            'examples.group.li3': 'Multiple aggregation functions combined',
            'examples.write.title': 'Flexible Output',
            'examples.write.desc': 'Query results can be mapped to Java objects, List, Map, or a single value — suits any use case.',
            'examples.write.li1': 'toList() → List<T>',
            'examples.write.li2': 'one() → T',
            'examples.write.li3': 'toMaps() → List<Map>',
            'examples.write.li4': 'toMap() → Map',
            'arch.tag': 'Architecture',
            'arch.title': 'Clean Layered Design',
            'arch.desc': 'Query building and execution engine are decoupled, interface-isolated, and flexibly extensible',
            'arch.layer1': 'Fluent API Layer',
            'arch.layer2': 'Query Model Layer',
            'arch.layer3': 'Execution Engine Layer',
            'arch.layer4': 'Lambda Parsing Layer',
            'scenarios.tag': 'Use Cases',
            'scenarios.title': 'Where to Use Linq.J?',
            'scenarios.desc': 'Distributed microservices, data processing, report generation… everywhere',
            'scenarios.card1.title': 'RPC Result Joining',
            'scenarios.card1.desc': 'JOIN multiple result sets from Feign / Dubbo / gRPC microservice calls in-memory, replacing multiple database round-trips.',
            'scenarios.card2.title': 'Heterogeneous Data Computation',
            'scenarios.card2.desc': 'Load results from Redis, MongoDB, MySQL and other sources into memory, then cross-source query and aggregate with Linq.J.',
            'scenarios.card3.title': 'SQL Result Post-Processing',
            'scenarios.card3.desc': 'Need further sorting, filtering, or aggregation on database results? Linq.J replaces complex secondary SQL with clear, maintainable code.',
            'scenarios.card4.title': 'In-Memory Pagination & Sorting',
            'scenarios.card4.desc': 'Sort, aggregate, and paginate merged result sets in memory — no more hand-written for loops and if branches, 80% less code.',
            'scenarios.card5.title': 'Object Transformation',
            'scenarios.card5.desc': 'Complete object transformation and mapping semantically — code is documentation, team collaboration is clearer and more agile.',
            'scenarios.card6.title': 'Federated Cross-Source Queries',
            'scenarios.card6.desc': 'Achieve federated access across different systems and stores at the application layer with Linq.J — no data migration needed.',
            'start.tag': 'Quick Start',
            'start.title': 'Three Steps to Linq.J',
            'start.desc': 'Zero configuration, ready to use immediately',
            'start.step1': 'Add Maven Dependency',
            'start.step2': 'Ensure Fields Have Getters',
            'start.step3': 'Start Querying',
            'start.copy': 'Copy',
            'start.copied': 'Copied!',
            'comparison.tag': 'Comparison',
            'comparison.title': 'Traditional vs Linq.J',
            'comparison.before': 'Traditional Java',
            'comparison.after': 'Linq.J Way',
            'roadmap.tag': 'Roadmap',
            'roadmap.title': 'Continuously Evolving',
            'roadmap.desc': 'Linq.J is constantly improving — more powerful features are coming',
            'roadmap.done': 'Done',
            'roadmap.planned': 'Planned',
            'roadmap.item1.title': 'HAVING Support',
            'roadmap.item1.desc': 'Conditional filtering after group aggregation',
            'roadmap.item2.title': 'Group Column Formatting',
            'roadmap.item2.desc': 'Support expressions like group by date(created_at)',
            'roadmap.item3.title': 'Set Operations',
            'roadmap.item3.desc': 'UNION ALL, UNION, INTERSECT, EXCEPT, UNION BY NAME',
            'roadmap.item4.title': 'Window Functions',
            'roadmap.item4.desc': 'ROW_NUMBER, RANK, DENSE_RANK and other analytic functions',
            'footer.desc': 'In-memory Java object query language<br>Inspired by C# LINQ',
            'footer.res': 'Resources',
            'footer.related': 'Related',
            'footer.csharp': 'C# LINQ Docs',
            'arch.item.facade': 'Linq (Facade)',
            'arch.item.state': 'Dql (State)',
            'arch.item.abstract': 'Engine (Abstract)',
            'arch.item.default': 'EruptEngine (Default)',
            'code.basic.cmt1': '// String sorting and filtering',
            'code.basic.cmt2': '// Result: [C, B, B]',
            'code.basic.cmt3': '// Number sorting',
            'code.basic.cmt4': '// Result: [1, 2, 3, 5, 6, 7]',
            'code.join.cmt1': '// Left Join + multi-table Select',
            'code.join.cmt2': '// Inner Join + Where + Distinct',
            'code.where.cmt1': '// Exact match',
            'code.where.cmt2': '// Range query',
            'code.where.cmt3': '// Custom single-field condition',
            'code.where.cmt4': '// Multi-field combined condition',
            'code.write.cmt1': '// Write as object list',
            'code.write.cmt2': '// Write single object',
            'code.write.cmt3': '// Write as Map list',
            'code.write.cmt4': '// Write single Map',
            'code.compare.cmt1': '// Still need sorting... more code...',
            'code.compare.cmt2': '// Clear · Concise · Efficient',
            'code.start.cmt1': '// Lombok annotation',
            'page.title': 'Linq.J — In-Memory Object Query for Java',
        }
    };

    let currentLang = localStorage.getItem('linq-lang') || 'en';

    function applyLang(lang) {
        currentLang = lang;
        localStorage.setItem('linq-lang', lang);
        document.documentElement.lang = lang === 'zh' ? 'zh-CN' : 'en';
        const t = translations[lang];
        document.title = t['page.title'];
        document.querySelectorAll('[data-i18n]').forEach(el => {
            const key = el.dataset.i18n;
            if (t[key] === undefined) return;
            if ('i18nHtml' in el.dataset) {
                el.innerHTML = t[key];
            } else {
                el.textContent = t[key];
            }
        });
        const btn = document.getElementById('langToggle');
        if (btn) btn.textContent = lang === 'zh' ? 'EN' : '中文';
    }

    const langToggle = document.getElementById('langToggle');
    if (langToggle) {
        langToggle.addEventListener('click', () => applyLang(currentLang === 'zh' ? 'en' : 'zh'));
    }

    applyLang(currentLang);

    // --- Navbar scroll effect ---
    const nav = document.getElementById('nav');
    const onScroll = () => {
        nav.classList.toggle('scrolled', window.scrollY > 20);
    };
    window.addEventListener('scroll', onScroll, { passive: true });
    onScroll();

    // --- Mobile menu toggle ---
    const toggle = document.getElementById('navToggle');
    const links = document.getElementById('navLinks');
    toggle.addEventListener('click', () => {
        links.classList.toggle('open');
        const spans = toggle.querySelectorAll('span');
        const isOpen = links.classList.contains('open');
        spans[0].style.transform = isOpen ? 'rotate(45deg) translate(5px, 5px)' : '';
        spans[1].style.opacity = isOpen ? '0' : '1';
        spans[2].style.transform = isOpen ? 'rotate(-45deg) translate(5px, -5px)' : '';
    });

    links.querySelectorAll('a').forEach(a => {
        a.addEventListener('click', () => {
            links.classList.remove('open');
            const spans = toggle.querySelectorAll('span');
            spans[0].style.transform = '';
            spans[1].style.opacity = '1';
            spans[2].style.transform = '';
        });
    });

    // --- Tab switching ---
    const tabBtns = document.querySelectorAll('.tab-btn');
    const tabPanels = document.querySelectorAll('.tab-panel');

    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const target = btn.dataset.tab;
            tabBtns.forEach(b => b.classList.remove('active'));
            tabPanels.forEach(p => p.classList.remove('active'));
            btn.classList.add('active');
            document.getElementById(target).classList.add('active');
        });
    });

    // --- Scroll reveal (data-aos) ---
    const reveals = document.querySelectorAll('[data-aos]');
    const revealObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                revealObserver.unobserve(entry.target);
            }
        });
    }, { threshold: 0.15, rootMargin: '0px 0px -40px 0px' });

    reveals.forEach(el => revealObserver.observe(el));

    // --- Copy button ---
    document.querySelectorAll('.copy-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.dataset.copy;
            const el = document.getElementById(id);
            if (!el) return;
            const text = el.textContent;
            navigator.clipboard.writeText(text).then(() => {
                const orig = translations[currentLang]['start.copy'];
                btn.textContent = translations[currentLang]['start.copied'];
                btn.style.color = 'rgba(255,255,255,0.8)';
                btn.style.borderColor = 'rgba(255,255,255,0.3)';
                setTimeout(() => {
                    btn.textContent = orig;
                    btn.style.color = '';
                    btn.style.borderColor = '';
                }, 2000);
            });
        });
    });

    // --- Smooth active nav link highlight ---
    const sections = document.querySelectorAll('section[id]');
    const navAnchors = document.querySelectorAll('.nav-links a');

    const highlightNav = () => {
        const scrollY = window.scrollY + 100;
        sections.forEach(section => {
            const top = section.offsetTop;
            const height = section.offsetHeight;
            const id = section.getAttribute('id');
            if (scrollY >= top && scrollY < top + height) {
                navAnchors.forEach(a => {
                    a.style.color = '';
                    a.style.background = '';
                    if (a.getAttribute('href') === '#' + id) {
                        a.style.color = 'var(--black)';
                    }
                });
            }
        });
    };
    window.addEventListener('scroll', highlightNav, { passive: true });
});
