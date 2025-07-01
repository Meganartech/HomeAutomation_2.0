import InsideSidePanel from "../InsideSidePanel";
import Layout from "./Layout";

export default function InsideLayout({ activePage, insideActivePage, menu, children }) {
    return (
        <>
            <Layout activePage={activePage}>

                {/* Inside Panel */}
                <div style={{ width: '240px' }}>
                    <InsideSidePanel menu={menu} activePage={insideActivePage} />
                </div>

                {/* Inside Content */}
                <div style={{ width: 'calc(100% - 240px)', height: '100%', overflowY: 'hidden' }}>
                    {children}
                </div>

            </Layout>
        </>
    );
}