import Layout from './Layout';

export default function HomeLayout({ InsideContent }) {
    return (
        <>
            <Layout activePage={'Home'} activeState={'sidepanel-active'}>
                {/* Inside Right Panel */}
                <div style={{ width: '100%', height: '100%', overflow: 'hidden' }}>
                    {InsideContent}
                </div>
            </Layout>
        </>
    );
};