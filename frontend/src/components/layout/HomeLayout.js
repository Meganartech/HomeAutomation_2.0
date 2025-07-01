import Layout from './Layout';

export default function HomeLayout({ InsideContent }) {
    return (
        <>
            <Layout activePage={'Home'}>

                {/* Inside Content */}
                <div style={{ width: '100%', height: '100%', overflow: 'hidden' }}>
                    <InsideContent />
                </div>

            </Layout>
        </>
    );
};